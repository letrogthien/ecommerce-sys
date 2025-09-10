package com.chuadatten.user.kafka;

import lombok.Getter;
@Getter
public enum KafkaTopic {
    REGISTER("user-register"),
    FORGOT_PASSWORD("user-forgot-password"),
    CHANGE_PASSWORD("user-change-password"),
    SEND_OTP("user-otp"),
    STRANGE_DEVICE("user-strange-device");

    private final String topicName;

    KafkaTopic(String topicName) {
        this.topicName = topicName;
    }

}
