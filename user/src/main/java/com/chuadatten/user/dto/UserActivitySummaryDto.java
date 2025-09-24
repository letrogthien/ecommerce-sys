package com.chuadatten.user.dto;

import lombok.*;
import java.time.LocalDateTime;

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