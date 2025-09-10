package com.chuadatten.event;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PasswordResetEvent {
    private String email;
    private String url;
}
