package com.chuadatten.transaction.securities;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import com.chuadatten.transaction.common.RoleName;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class Security {
    private final CustomJwtDecoder jwtDecoder;
    private final CustomAuthenticatinConverter converter;
    private final CustomAuthenticationEntryPoint entryPoint;
    private final GetTokenResolver getTokenResolver;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
        http.cors(cors -> cors
            .configurationSource(request -> {
                org.springframework.web.cors.CorsConfiguration config = new org.springframework.web.cors.CorsConfiguration();
                config.setAllowedOrigins(java.util.List.of("https://wezd.io.vn"));
                config.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                config.setAllowedHeaders(java.util.List.of("*"));
                config.setAllowCredentials(true);
                return config;
            })
        );

        http.authorizeHttpRequests(auth -> auth
            // --- Public ---
            
            .requestMatchers("/swagger-ui/**").permitAll()
            .requestMatchers("/v3/api-docs/**").permitAll()
            // --- ADMIN ---
            .requestMatchers("/api/v1/transaction-service/admin/**").hasAuthority(RoleName.ROLE_ADMIN.name())

            // --- ANALYTICS (Seller, Admin) ---
            .requestMatchers("/api/v1/transaction-service/analytics/**").hasAnyAuthority(RoleName.ROLE_SELLER.name(), RoleName.ROLE_ADMIN.name())

            // --- USER (Buyer) ---
            .requestMatchers(HttpMethod.POST, "/api/v1/transaction-service/orders").hasAuthority(RoleName.ROLE_USER.name())
            .requestMatchers(HttpMethod.GET, "/api/v1/transaction-service/orders/buyer").hasAuthority(RoleName.ROLE_USER.name())
            .requestMatchers(HttpMethod.PUT, "/api/v1/transaction-service/orders/*/cancel").hasAuthority(RoleName.ROLE_USER.name())
            .requestMatchers(HttpMethod.POST, "/api/v1/transaction-service/refunds").hasAuthority(RoleName.ROLE_USER.name())
            .requestMatchers(HttpMethod.GET, "/api/v1/transaction-service/refunds/order/*").hasAuthority(RoleName.ROLE_USER.name())
            .requestMatchers(HttpMethod.GET, "/api/v1/transaction-service/refunds/buyer").hasAuthority(RoleName.ROLE_USER.name())

            // --- SELLER ---
            .requestMatchers(HttpMethod.GET, "/api/v1/transaction-service/orders/seller").hasAuthority(RoleName.ROLE_SELLER.name())
            .requestMatchers(HttpMethod.POST, "/api/v1/transaction-service/orders/*/proof").hasAuthority(RoleName.ROLE_SELLER.name())
            .requestMatchers("/api/v1/transaction-service/refunds/seller/**").hasAnyAuthority(RoleName.ROLE_SELLER.name(), RoleName.ROLE_ADMIN.name())
            
            // --- SELLER REFUND MANAGEMENT ---
            .requestMatchers(HttpMethod.GET, "/api/v1/transaction-service/refunds/seller/*").hasAnyAuthority(RoleName.ROLE_SELLER.name(), RoleName.ROLE_ADMIN.name())
            .requestMatchers(HttpMethod.PUT, "/api/v1/transaction-service/refunds/*/approve").hasAnyAuthority(RoleName.ROLE_SELLER.name(), RoleName.ROLE_ADMIN.name())
            .requestMatchers(HttpMethod.PUT, "/api/v1/transaction-service/refunds/*/reject").hasAnyAuthority(RoleName.ROLE_SELLER.name(), RoleName.ROLE_ADMIN.name())
            .requestMatchers(HttpMethod.PUT, "/api/v1/transaction-service/refunds/*/status").hasAuthority(RoleName.ROLE_ADMIN.name())
            .requestMatchers(HttpMethod.POST, "/api/v1/transaction-service/refunds/*/process-payment").hasAuthority(RoleName.ROLE_ADMIN.name())

            // --- USER OR SELLER ---
            .requestMatchers(HttpMethod.GET, "/api/v1/transaction-service/orders/*").hasAnyAuthority(RoleName.ROLE_USER.name(), RoleName.ROLE_SELLER.name())
            .requestMatchers(HttpMethod.POST, "/api/v1/transaction-service/disputes").hasAnyAuthority(RoleName.ROLE_USER.name(), RoleName.ROLE_SELLER.name())
            .requestMatchers(HttpMethod.GET, "/api/v1/transaction-service/disputes/order/*").hasAnyAuthority(RoleName.ROLE_USER.name(), RoleName.ROLE_SELLER.name())
            .requestMatchers(HttpMethod.PUT, "/api/v1/transaction-service/disputes/*").hasAnyAuthority(RoleName.ROLE_USER.name(), RoleName.ROLE_SELLER.name())

            // --- Default ---
            .anyRequest().authenticated()
        );

        http.sessionManagement(session ->
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        http.oauth2ResourceServer(oauth2 -> oauth2
            .jwt(jwt -> jwt
                .decoder(jwtDecoder)
                .jwtAuthenticationConverter(converter)
            )
            .bearerTokenResolver(getTokenResolver)
            .authenticationEntryPoint(entryPoint)
        );

        return http.build();
    }
}
