package com.chuadatten.transaction.request;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundApproveRequest {
    
    @NotNull(message = "Seller ID is required")
    private UUID sellerId;
    
    private String note; // Optional seller note
}