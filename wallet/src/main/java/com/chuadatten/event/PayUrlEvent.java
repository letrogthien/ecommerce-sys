package com.chuadatten.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PayUrlEvent {
    private String orderId;
    private String paymentId;
    private String payUrl;
    private String userId;
}
