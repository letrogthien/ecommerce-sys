package com.chuadatten.user.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginHistoryDto {
    private UUID id;
    private UUID userId;
    private String username;
    private LocalDateTime loginAt;
    private String ipAddress;
    private String deviceInfo;
    private Boolean success;
}