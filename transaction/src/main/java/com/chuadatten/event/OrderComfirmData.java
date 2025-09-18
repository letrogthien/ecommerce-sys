package com.chuadatten.event;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderComfirmData {
    String orderId;
    String buyerId;
    BigDecimal totalAmount;
    String idempotencyKey;
}
