package com.chuadatten.user.securities;

import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;


@Component
public class GetTokenResolver implements BearerTokenResolver {
    @Override
    public String resolve(HttpServletRequest request) {
        System.out.println("Resolving token for request URI: " + request.getCookies());
        // List of public endpoints to skip token check
        String uri = request.getRequestURI();
        String[] publicEndpoints = {
            "/api/v1/user-service/auth/login",
            "/api/v1/user-service/auth/register",
            "/api/v1/user-service/auth/verify-2fa",
            "/api/v1/user-service/auth/trust-device",
            "/api/v1/user-service/auth/reset-password",
            "/api/v1/user-service/auth/forgot-password",
            "/api/v1/user-service/auth/activate-account",
            "/api/v1/user-service/auth/access-token",
            "/api/v1/user-service/search/user/name",
            "/api/v1/user-service/auth/cleaer-cookie",
            "/swagger-ui/",
            "/v3/api-docs/",
            "/api/v1/oauth2/jwks",
            "/api/v1/user-service/auth/test"
        };
        for (String endpoint : publicEndpoints) {
            if (uri.startsWith(endpoint)) {
                return null;
            }
        }
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        if (request.getCookies()!= null){
            for (var cookie : request.getCookies()) {
                if ("access_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
