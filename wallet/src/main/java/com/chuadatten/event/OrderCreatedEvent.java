package com.chuadatten.event;

import java.math.BigDecimal;
import java.util.List;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor  
public class OrderCreatedEvent {
    private String orderId;
    private String buyerId;
    private String sellerId;
    private BigDecimal totalAmount;
    private String currency;
    private List<OrderItemEvent> items;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderItemEvent {
        private String productId;
        private String productVariantId;
        private int quantity;
        private BigDecimal unitPrice;
    }
}