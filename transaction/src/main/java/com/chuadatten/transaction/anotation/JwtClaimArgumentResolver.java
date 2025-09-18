package com.chuadatten.transaction.anotation;

import java.util.UUID;

import com.chuadatten.transaction.exceptions.CustomException;
import com.chuadatten.transaction.exceptions.ErrorCode;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class JwtClaimArgumentResolver implements HandlerMethodArgumentResolver {

    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(JwtClaims.class);
    }

    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        JwtClaims jwtClaim = parameter.getParameterAnnotation(JwtClaims.class);

        assert jwtClaim != null;

        String claimName = jwtClaim.value();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            Object claimValue = jwt.getClaim(claimName);
            if (claimValue == null) {
                throw new CustomException(ErrorCode.INVALID_CLAIM);
            } else {
                return this.convertClaimValue(claimName, claimValue, parameter.getParameterType());
            }
        } else {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
    }

    private Object convertClaimValue(String claimName, Object claimValue, Class<?> targetType) {
        if ("id".equals(claimName)) {
            try {
                return UUID.fromString((String) claimValue);
            } catch (IllegalArgumentException var5) {
                throw new CustomException(ErrorCode.INVALID_CLAIM);
            }
        } else if (targetType == String.class && claimValue instanceof String) {
            return claimValue;
        } else {
            throw new IllegalArgumentException("Cannot convert claim '" + claimName + "' to " + targetType.getName());
        }
    }
}
