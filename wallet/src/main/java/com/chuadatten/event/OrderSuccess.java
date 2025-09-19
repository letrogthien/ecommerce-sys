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
public class OrderSuccess {
    private String orderId;
    private String sellerId; 
    private List<ProductQuantityUpdate> products;
    private BigDecimal finalAmount;
    private String currency;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductQuantityUpdate {
        private String productId;
        private String productVariantId;
        private int quantity;
        private BigDecimal pricePerUnit;
        private BigDecimal subtotal;
    }
}