package com.chuadatten.event;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OtpEvent {
    private String otp;
    private String email;
    private String otpType;
}
