package com.chuadatten.notify.socket;

import java.util.Map;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;


@Configuration
public class JwtHandshakeInterceptor extends HttpSessionHandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {

        String token = null;

        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpRequest = servletRequest.getServletRequest();

            // Try to get token from multiple sources
            token = extractTokenFromRequest(httpRequest);
        }

        if (token == null) {
            System.err.println("UNAUTHORIZED: Missing access_token");
            return false; // Reject handshake instead of throwing exception
        }

        try {
            // Validate and extract userId
            String userId = this.extractClaim(token, "id");
            
            // Lưu userId vào attributes để gắn với WebSocket session
            attributes.put("userId", userId);
            attributes.put("token", token);
            
            System.out.println("WebSocket handshake successful for user: " + userId);
            
        } catch (Exception e) {
            System.err.println("Token validation failed: " + e.getMessage());
            return false; // Reject handshake
        }

        return super.beforeHandshake(request, response, wsHandler, attributes);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        // 1. Try cookie first
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("access_token".equals(cookie.getName())) {
                    System.out.println("Found access_token in cookie");
                    return cookie.getValue();
                }
            }
        }

        // 2. Try Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            System.out.println("Found access_token in Authorization header");
            return authHeader.substring(7);
        }

        // 3. Try query parameter (less secure, use with caution)
        String tokenParam = request.getParameter("token");
        if (tokenParam != null) {
            System.out.println("Found access_token in query parameter");
            return tokenParam;
        }

        return null;
    }

    private Map<String, Object> extractClaims(String token) {
        try {
            JWT jwt = JWTParser.parse(token);
            return jwt.getJWTClaimsSet().getClaims();
        } catch (Exception e) {
            throw new RuntimeException("UNAUTHORIZED: Invalid token - " + e.getMessage());
        }
    }

    private String extractClaim(String token, String claim) {
        Map<String, Object> claims = this.extractClaims(token);
        if (claims.containsKey(claim)) {
            return claims.get(claim).toString();
        } else {
            throw new RuntimeException("UNAUTHORIZED: Missing claim " + claim);
        }
    }
}
