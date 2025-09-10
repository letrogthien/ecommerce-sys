package com.chuadatten.event;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentSuccessedEvent {
    private String orderId;
    private String paymentId;
    private String userId;
}
