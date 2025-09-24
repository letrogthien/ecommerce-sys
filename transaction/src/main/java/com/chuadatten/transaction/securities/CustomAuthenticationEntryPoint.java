package com.chuadatten.transaction.securities;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.chuadatten.transaction.exceptions.CustomException;
import com.chuadatten.transaction.exceptions.ErrorCode;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        CustomException jwtException = new CustomException(ErrorCode.UNAUTHORIZED);

        Throwable cause = authException.getCause();
        if (cause instanceof JwtException) {
            jwtException = new CustomException(ErrorCode.INVALID_TOKEN);
        }
        String responseBody = jwtException.toString();
        try (ServletOutputStream outputStream = response.getOutputStream()) {
            outputStream.write(responseBody.getBytes());
            outputStream.flush();
        }
    }
}