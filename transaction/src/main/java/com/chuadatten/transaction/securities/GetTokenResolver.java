package com.chuadatten.transaction.securities;

import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;


@Component
public class GetTokenResolver implements BearerTokenResolver {

    @Override
    public String resolve(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String[] publicEndpoints = {

                "/swagger-ui/",
                "/v3/api-docs/",

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
                    System.out.println("Found access token in cookie" + cookie.getValue());
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
