package com.chuadatten.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentCancelEvent {
    int status = 0; // 0: failed, 1: success
    String orderId;
    String paymentId;
    int reasonCode; 
}
