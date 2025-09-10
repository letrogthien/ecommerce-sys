package com.chuadatten.wallet.kafka;


import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(collection = "send_message_error")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendMessageError {

    @Id
    private String id;

    @Field("topic")
    private String topic;

    @Field("message")
    private String message;

    @Field("status")
    private String status;

    @CreatedDate
    @Field("created_at")
    private Instant createdAt;
}
