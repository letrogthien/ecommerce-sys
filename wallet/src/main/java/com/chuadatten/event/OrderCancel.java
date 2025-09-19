package com.chuadatten.event;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderCancel {
    private String orderId;
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
