package com.chuadatten.product.securities;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import com.chuadatten.product.common.RoleName;

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
                config.setAllowedOrigins(java.util.List.of("https://wezd.io.vn", "https://admin.wezd.io.vn", "https://product.wezd.io.vn"));
                config.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                config.setAllowedHeaders(java.util.List.of("*"));
                config.setAllowCredentials(true);
                return config;
            })
        );

        http.authorizeHttpRequests(auth -> auth
            // --- Public ---
            .requestMatchers(HttpMethod.GET, "/api/v1/product-service/products/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/v1/product-service/categories/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/v1/product-service/product-variants/**").permitAll()
            .requestMatchers("/api/v1/product-service/search/**").permitAll()
            .requestMatchers("/files/**").permitAll()

            // --- USER ---
            .requestMatchers(HttpMethod.POST, "/api/v1/product-service/product-variants/*/reserve").hasAuthority(RoleName.ROLE_USER.name())
            .requestMatchers(HttpMethod.POST, "/api/v1/product-service/product-variants/*/release").hasAuthority(RoleName.ROLE_USER.name())
            .requestMatchers(HttpMethod.POST, "/api/v1/product-service/product-variants/*/commit").hasAuthority(RoleName.ROLE_USER.name())

            // --- SELLER ---
            .requestMatchers(HttpMethod.POST, "/api/v1/product-service/products").hasAuthority(RoleName.ROLE_SELLER.name())
            .requestMatchers(HttpMethod.PUT, "/api/v1/product-service/products/*").hasAuthority(RoleName.ROLE_SELLER.name())
            .requestMatchers(HttpMethod.DELETE, "/api/v1/product-service/products/*").hasAuthority(RoleName.ROLE_SELLER.name())
            .requestMatchers(HttpMethod.POST, "/api/v1/product-service/products/*/images").hasAuthority(RoleName.ROLE_SELLER.name())

            .requestMatchers(HttpMethod.POST, "/api/v1/product-service/product-variants").hasAuthority(RoleName.ROLE_SELLER.name())
            .requestMatchers(HttpMethod.PUT, "/api/v1/product-service/product-variants/*").hasAuthority(RoleName.ROLE_SELLER.name())
            .requestMatchers(HttpMethod.DELETE, "/api/v1/product-service/product-variants/*").hasAuthority(RoleName.ROLE_SELLER.name())

            // --- ADMIN ---
            .requestMatchers("/api/v1/product-service/admin/**").hasAuthority(RoleName.ROLE_ADMIN.name())

            .requestMatchers("/swagger-ui/**").permitAll()
            .requestMatchers("/v3/api-docs/**").permitAll()

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
