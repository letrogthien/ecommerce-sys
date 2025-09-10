package com.chuadatten.transaction.outbox;

import lombok.*;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Document(collection = "outbox_event")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxEvent {

    @Id
    private String id;

    @Field("aggregate_type")
    private String aggregateType;

    @Field("aggregate_id")
    @Indexed
    private String aggregateId;

    @Field("event_type")
    @Indexed
    private String eventType;

    private String payload;

    private String headers;

    @Builder.Default
    @Indexed
    private String status = "PENDING";

    @Builder.Default
    private int attempts = 0;

    @Field("created_at")
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Field("published_at")
    @Indexed
    private Instant publishedAt;
}
