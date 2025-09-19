package com.chuadatten.wallet.securities;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

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
                config.setAllowedOrigins(java.util.List.of("https://pay.wezd.io.vn", "https://admin.wezd.io.vn", "https://wezd.io.vn", "http://localhost:5173"));
                config.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                config.setAllowedHeaders(java.util.List.of("*"));
                config.setAllowCredentials(true);
                return config;
            })
        );

        http.authorizeHttpRequests(auth -> auth


            // --- Default ---
            .anyRequest().permitAll()
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
