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
public class RefundProcessPaymentRequest {
    
    @NotNull(message = "Admin ID is required")
    private UUID adminId;
    
    @NotNull(message = "Payment method is required")
    private String paymentMethod; // WALLET, BANK_TRANSFER, ORIGINAL_METHOD
    
    private String note; // Optional note
}