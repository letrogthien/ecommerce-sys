package com.chuadatten.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentProcessEvent {
    private String paymentId;
    private String orderId;
    private String ip;
    private String paymentMethod;
}