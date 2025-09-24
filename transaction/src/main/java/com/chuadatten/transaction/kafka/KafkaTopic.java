package com.chuadatten.transaction.kafka;

import lombok.Getter;

@Getter
public enum KafkaTopic {

    ORDER_CREATED("transaction.order.created"),
    PRODUCT_RESERVATION_SUCCESS("product.reservation.success"),
    PRODUCT_RESERVATION_FAILED("product.reservation.failed"),
    PAYMENT_PROCESSING("payment.payment.processing"),
    PAYMENT_SUCCESS("payment.success"),
    ORDER_COMFIRM_DATA("transaction.order.confirm.data"),
    CHECKING_ORDER_RETURN("order.checking.status"),
    ORDER_SUCCESS("transaction.order.success"),
    ORDER_CANCEL("transaction.order.cancel"),
    CLEAN_UP_ORDER("transaction.clean.up.order");

    private final String topicName;

    KafkaTopic(String topicName) {
        this.topicName = topicName;
    }

}
