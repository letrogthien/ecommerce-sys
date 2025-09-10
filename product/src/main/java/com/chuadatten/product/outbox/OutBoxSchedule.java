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
    public void pollAndPublishReservationFailed() {
        pollAndPublishHelper(KafkaTopic.PRODUCT_RESERVATION_FAILED.name());
    }

    @Scheduled(initialDelay = 5000, fixedDelay = 1000)
    public void pollAndPublishReservationSuccessed() {
        pollAndPublishHelper(KafkaTopic.PRODUCT_RESERVATION_SUCCESS.name());
    }

    private void pollAndPublishHelper(String topicName) {
        List<OutboxEvent> batch = lockNextBatch(BATCH_SIZE, topicName);
        if (batch.isEmpty())
            return;

        for (OutboxEvent event : batch) {
            try {
                Object orderCreatedEvent = jsonParserUtil.fromJson(event.getPayload(),
                        Object.class);

                CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                        KafkaTopic.ORDER_CREATED.getTopicName(),
                        orderCreatedEvent);

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

    /**
     * Lock batch bằng findAndModify (tránh nhiều poller đụng cùng record)
     */
    private List<OutboxEvent> lockNextBatch(int size, String eventType) {
        List<OutboxEvent> locked = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            OutboxEvent e = mongoTemplate.findAndModify(
                    new Query(Criteria.where("status").is("PENDING").and("eventType").is(eventType))
                            .with(org.springframework.data.domain.Sort.by("createdAt").ascending()),
                    new Update().set("status", "PROCESSING").set("lockedAt", Instant.now()),
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
                new Update().set("status", "PUBLISHED").set("publishedAt", Instant.now()),
                OutboxEvent.class);
    }

    private void markFailed(OutboxEvent event, Throwable error) {
        mongoTemplate.updateFirst(
                new Query(Criteria.where("_id").is(event.getId())),
                new Update().set("status", "FAILED").inc("attempts", 1),
                OutboxEvent.class);
    }

}
