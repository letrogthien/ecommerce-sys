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
public class DeviceManagerDto {
    private UUID id;
    private UUID userId;
    private String username;
    private String deviceName;
    private String deviceType;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private Boolean isActive;
}