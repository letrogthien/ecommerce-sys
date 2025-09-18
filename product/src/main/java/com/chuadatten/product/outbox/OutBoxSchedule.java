package com.chuadatten.product.outbox;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.chuadatten.product.common.JsonParserUtil;
import com.chuadatten.product.kafka.KafkaTopic;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OutBoxSchedule {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final MongoTemplate mongoTemplate;
    private final JsonParserUtil jsonParserUtil;

    private static final int BATCH_SIZE = 100;

    /**
     * Poll và publish event order created sang Kafka
     */


    @Scheduled(initialDelay = 5000, fixedDelay = 1000)
    public void orderCreated() {
        pollAndPublish(KafkaTopic.ORDER_CREATED);
    }

    @Scheduled(initialDelay = 5000, fixedDelay = 1000)
    public void productReservationSuccess() {
        pollAndPublish(KafkaTopic.PRODUCT_RESERVATION_SUCCESS);
    }

    @Scheduled(initialDelay = 5000, fixedDelay = 1000)
    public void orderSuccess() {
        pollAndPublish(KafkaTopic.ORDER_SUCCESS);
    }

    @Scheduled(initialDelay = 5000, fixedDelay = 1000)
    public void productReservationFailed() {
        pollAndPublish(KafkaTopic.PRODUCT_RESERVATION_FAILED);
    }

    public void pollAndPublish(KafkaTopic topic) {
        List<OutboxEvent> batch = lockNextBatch(BATCH_SIZE, topic.name());
        if (batch.isEmpty())
            return;

        for (OutboxEvent event : batch) {
            try {
                Object eventObject = parserObject(topic, event.getPayload());
                CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                        topic.getTopicName(),
                        eventObject);

                future.whenComplete((result, ex) -> {
                    if (ex != null) {
                        markFailed(event, ex);
                    } else {
                        markPublished(event);
                    }
                });

            } catch (Exception ex) {
                markFailed(event, ex);
            }
        }
    }

    private Object parserObject(KafkaTopic topic, String payload) {
        switch (topic) {
            case ORDER_CREATED:
                return jsonParserUtil.fromJson(payload, com.chuadatten.event.OrderCreatedEvent.class);
            case PRODUCT_RESERVATION_SUCCESS:
                return jsonParserUtil.fromJson(payload, com.chuadatten.event.ProductReservationSuccessEvent.class);
            case PRODUCT_RESERVATION_FAILED:
                return jsonParserUtil.fromJson(payload, com.chuadatten.event.ProductReservationFailedEvent.class);
            case ORDER_SUCCESS:
                return jsonParserUtil.fromJson(payload, com.chuadatten.event.OrderSuccess.class);
            default:
                return jsonParserUtil.fromJson(payload, Object.class);
        }
    }

    /**
     * Lock batch bằng findAndModify (tránh nhiều poller đụng cùng record)
     */
    private List<OutboxEvent> lockNextBatch(int size, String eventType) {
        List<OutboxEvent> locked = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            OutboxEvent e = mongoTemplate.findAndModify(
                    new Query(Criteria.where("status").is("PENDING").and("event_type").is(eventType))
                            .with(org.springframework.data.domain.Sort.by("created_at").ascending()),
                    new Update().set("status", "PROCESSING").set("locked_at", Instant.now()),
                    org.springframework.data.mongodb.core.FindAndModifyOptions.options().returnNew(true),
                    OutboxEvent.class);
            if (e == null)
                break;
            locked.add(e);
        }
        return locked;
    }

    private void markPublished(OutboxEvent event) {
        mongoTemplate.updateFirst(
                new Query(Criteria.where("_id").is(event.getId())),
                new Update().set("status", "PUBLISHED").set("published_at", Instant.now()),
                OutboxEvent.class);
    }

    private void markFailed(OutboxEvent event, Throwable error) {
        mongoTemplate.updateFirst(
                new Query(Criteria.where("_id").is(event.getId())),
                new Update().set("status", "FAILED").inc("attempts", 1),
                OutboxEvent.class);
    }

}
