package com.chuadatten.user.controller;

import java.security.interfaces.RSAPublicKey;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("api/v1/oauth2")
@RequiredArgsConstructor
public class JwksController {

    private final RSAPublicKey publicKey;

    @GetMapping("/jwks")
    public Map<String, Object> getJwks() {
        RSAKey key = new RSAKey.Builder(publicKey)
                .keyUse(KeyUse.SIGNATURE)
                .algorithm(JWSAlgorithm.RS256)
                .keyID("auth-key-001")
                .build();
        return new JWKSet(key).toJSONObject();
    }
}