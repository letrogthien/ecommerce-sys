package com.chuadatten.wallet.kafka;

import lombok.Getter;

@Getter
public enum KafkaTopic {

    ORDER_CREATED("transaction.order.created"),
    PRODUCT_RESERVATION_SUCCESS("product.reservation.success"),
    PRODUCT_RESERVATION_FAILED("product.reservation.failed"),
    ORDER_COMFIRM_DATA("transaction.order.confirm.data"),
    PAYMENT_PROCECSSING("payment.payment.processing"),
    CHECKING_ORDER_RETURN("order.checking.status"),
    PAYMENT_URL_SUCCESS("payment.url.success"), 
    PAYMENT_SUCCESS("payment.success"),
    ORDER_CANCEL("transaction.order.cancel"),
    ORDER_SUCCESS("transaction.order.success"),
    PAYMENT_CANCEL_SUCCESS("payment.cancel.success"),
    PAYMENT_CANCEL_FAILED("payment.cancel.failed");

    private final String topicName;

    KafkaTopic(String topicName) {
        this.topicName = topicName;
    }

}
