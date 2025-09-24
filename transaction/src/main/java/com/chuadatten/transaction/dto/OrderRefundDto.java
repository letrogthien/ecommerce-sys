
package com.chuadatten.transaction.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.chuadatten.transaction.common.Status;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRefundDto {
    private UUID id;
    private UUID orderId;
    private UUID requestBy;
    private BigDecimal amount; // From orders.total_amount via JOIN
    private String currency; // From orders.currency via JOIN
    private Status status;
    private String reason;
    private String adminNote; // For admin notes
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private LocalDateTime updatedAt; // For tracking updates
    private UUID updatedBy; // Who made the last update
    
    // Order information from JOIN
    private OrderInfoDto order;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderInfoDto {
        private BigDecimal totalAmount;
        private String currency;
        private UUID buyerId;
        private UUID sellerId;
        private Status orderStatus;
        private LocalDateTime orderCreatedAt;
    }
}
