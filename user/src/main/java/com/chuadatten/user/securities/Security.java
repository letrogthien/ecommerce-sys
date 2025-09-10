package com.chuadatten.user.securities;



import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import com.chuadatten.user.common.RoleName;

import lombok.RequiredArgsConstructor;


@Configuration
@RequiredArgsConstructor
public class Security {
    private final CustomJwtDecoder jwtDecoder;
    private final CustomAuthenticatinConverter converter;
    private final CustomAuthenticationEntryPoint entryPoint;
    private final GetTokenResolver getTokenResolver;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity security) throws Exception {
        security.csrf(AbstractHttpConfigurer::disable);
        configureAuthorizationRules(security);
        configureSessionManagement(security);
        configureOAuth2ResourceServer(security);
        return security.build();
    }

    private void configureAuthorizationRules(HttpSecurity security) throws Exception {
        security.authorizeHttpRequests(authorize ->
            authorize
                .requestMatchers(
                    "/api/v1/user-service/admin/**",
                    "/api/v1/user-service/v1/auth/assign-role"
                ).hasAuthority(RoleName.ROLE_ADMIN.name())

                .requestMatchers(
                    "/api/v1/user-service/seller/**"
                ).hasAnyAuthority(RoleName.ROLE_SELLER.name(), RoleName.ROLE_ADMIN.name())

                .requestMatchers(
                    "/api/v1/user-service/users/me/**",
                    "/api/v1/user-service/kyc/**",
                    "/api/v1/user-service/files/**",
                    "/api/v1/user-service/auth/logout",
                    "/api/v1/user-service/auth/logout-all",
                    "/api/v1/user-service/auth/enable-2fa",
                    "/api/v1/user-service/auth/disable-2fa",
                    "/api/v1/user-service/auth/change-password"
                ).hasAnyAuthority(RoleName.ROLE_USER.name(), RoleName.ROLE_SELLER.name(), RoleName.ROLE_ADMIN.name())

                .requestMatchers(
                    "/api/v1/user-service/auth/login",
                    "/api/v1/user-service/auth/register",
                    "/api/v1/user-service/auth/verify-2fa",
                    "/api/v1/user-service/auth/trust-device",
                    "/api/v1/user-service/auth/reset-password",
                    "/api/v1/user-service/auth/forgot-password",
                    "/api/v1/user-service/auth/activate-account",
                    "/api/v1/user-service/auth/access-token",
                    "/api/v1/user-service/search/user/name",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/api/v1/user-service/oauth2/jwks",
                    "/api/v1/user-service/auth/test"
                ).permitAll()
                .requestMatchers(HttpMethod.GET,"/api/v1/user-service/users/**")
                .permitAll()

                .anyRequest().authenticated()
        );
    }

    private void configureSessionManagement(HttpSecurity security) throws Exception {
        security.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    }

    private void configureOAuth2ResourceServer(HttpSecurity security) throws Exception {
        security.oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                        .decoder(jwtDecoder)
                        .jwtAuthenticationConverter(converter)
                )
                .bearerTokenResolver(getTokenResolver)
                .authenticationEntryPoint(entryPoint)
        );
    }

}
