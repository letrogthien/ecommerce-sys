package com.chuadatten.user.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDto {
    private UUID id;
    private UUID userId;
    private String action;
    private String description;
    private LocalDateTime createdAt;
}