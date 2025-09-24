package com.chuadatten.user.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageErrorDto {
    private UUID id;
    private String topic;
    private String message;
    private String errorType;
    private String errorMessage;
    private Integer retryCount;
    private LocalDateTime createdAt;
    private Boolean resolved;
}