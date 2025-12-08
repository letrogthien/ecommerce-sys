package com.chuadatten.user.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserActivitySummaryDto {
    private Long loginCount;
    private Long registrationCount;
    private Long kycSubmissions;
    private Long sellerApplications;
    private Long transactionCount;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
}