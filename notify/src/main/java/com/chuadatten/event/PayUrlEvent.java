package com.chuadatten.event;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PayUrlEvent {
    private String orderId;
    private String paymentId;
    private String payUrl;
    private String userId;
}
