package com.chuadatten.product.kafka;

import lombok.Getter;

@Getter
public enum KafkaTopic {
    ORDER_CREATED("transaction.order.created"),
    PRODUCT_RESERVATION_SUCCESS("product.reservation.success"),
    ORDER_SUCCESS("transaction.order.success"),
    PRODUCT_RESERVATION_FAILED("product.reservation.failed");

    private final String topicName;

    KafkaTopic(String topicName) {
        this.topicName = topicName;
    }
}
