package com.chuadatten.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OtpEvent {
    private String otp;
    private String email;
    private String otpType;
}
