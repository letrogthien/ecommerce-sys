package com.chuadatten.user.common;

import lombok.Getter;

@Getter
public enum ConstString {
    DOMAIN_NAME("https://auth.wezd.io.vn/api/v1/auth/"),
    FE_DOMAIN("https://wezd.io.vn/activate");

    private final String value;
    private ConstString(String value) {
        this.value = value;
    }

}