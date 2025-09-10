package com.chuadatten.event;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentProcessEvent {
    private String paymentId;
    private String orderId;
    private String ip;
    private String paymentMethod;
}