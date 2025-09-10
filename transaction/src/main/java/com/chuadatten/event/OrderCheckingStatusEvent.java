package com.chuadatten.event;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderCheckingStatusEvent {
    private String orderId;
    private String paymentId;
    private String status;
    private String ip;
    private String paymentMethod;
}
