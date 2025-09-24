package com.chuadatten.transaction.request;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundRejectRequest {
    
    @NotNull(message = "Seller ID is required")
    private UUID sellerId;
    
    @NotBlank(message = "Rejection reason is required")
    private String reason; // Required reason for rejection
}