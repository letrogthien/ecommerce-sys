package com.chuadatten.user.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

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