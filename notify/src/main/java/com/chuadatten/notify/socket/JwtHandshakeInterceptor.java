package com.chuadatten.notify.socket;

import java.util.Map;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;


@Configuration
public class JwtHandshakeInterceptor extends HttpSessionHandshakeInterceptor {
    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {
        // Lấy JWT từ query param hoặc header
        String token = request.getURI().getQuery().replace("token=", "");
        // parse JWT -> userId
        String userId = this.extractClaim(token, "id");
        // Lưu userId vào attributes
        attributes.put("id", userId);
        return super.beforeHandshake(request, response, wsHandler, attributes);
    }

        private Map<String, Object> extractClaims(String token) {
        try {
            JWT jwt = JWTParser.parse(token);
            return jwt.getJWTClaimsSet().getClaims();
        } catch (Exception var3) {
            throw new RuntimeException("UNAUTHORIZED");
        }
    }

    private String extractClaim(String token, String claim) {
        Map<String, Object> claims = this.extractClaims(token);
        if (claims.containsKey(claim)) {
            return claims.get(claim).toString();
        } else {
            throw new RuntimeException("UNAUTHORIZED");
        }
    }
}