package com.chuadatten.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderCheckingStatusEvent {
    private String orderId;
    private String paymentId;
    private String status;
    private String ip;
    private String paymentMethod;
}
