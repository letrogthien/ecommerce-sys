package com.chuadatten.user.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.chuadatten.user.common.Status;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KycDto {
    private UUID id;
    private UUID userId;
    private String username;
    private String email;
    private Status verificationStatus;
    private String frontIdUrl;
    private String backIdUrl;
    private String selfieUrl;
    private String rejectionReason;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
}