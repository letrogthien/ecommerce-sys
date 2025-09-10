package com.chuadatten.event;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderComfirmData {
    String orderId;
    BigDecimal totalAmount;
    String idempotencyKey;
    String buyerId;
}
