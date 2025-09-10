package com.chuadatten.notify.kafka;

import lombok.Getter;
@Getter
public enum KafkaTopic {
    REGISTER("user-register"),
    FORGOT_PASSWORD("user-forgot-password"),
    CHANGE_PASSWORD("user-change-password"),
    SEND_OTP("user-otp"),
    STRANGE_DEVICE("user-strange-device"),
    PAYMENT_URL_SUCCESS("payment.url.success");

    private final String topicName;

    KafkaTopic(String topicName) {
        this.topicName = topicName;
    }

}
