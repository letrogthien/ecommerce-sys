package com.chuadatten.product.kafka;

import lombok.Getter;

@Getter
public enum KafkaTopic {
    ORDER_CREATED("transaction.order.created"),
    PRODUCT_RESERVATION_SUCCESS("product.reservation.success"),
    ORDER_SUCCESS("transaction.order.success"),
    PRODUCT_RESERVATION_FAILED("product.reservation.failed"),

    ORDER_CANCEL("transaction.order.cancel"),
    PAYMENT_CANCEL_SUCCESS("payment.cancel.success"),

    PAYMENT_CANCEL_FAILED("payment.cancel.failed"),
    CLEAN_UP_ORDER("transaction.clean.up.order");

    private final String topicName;

    KafkaTopic(String topicName) {
        this.topicName = topicName;
    }
}
