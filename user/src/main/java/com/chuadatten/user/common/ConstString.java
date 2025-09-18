package com.chuadatten.user.common;

import lombok.Getter;

@Getter
public enum ConstString {
    DOMAIN_NAME("http://localhost:8082/api/v1/auth/"),
    FE_DOMAIN("http://localhost:5173/activate");

    private final String value;
    private ConstString(String value) {
        this.value = value;
    }

}