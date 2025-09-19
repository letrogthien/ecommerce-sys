package com.chuadatten.wallet.outbox;

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

import com.chuadatten.wallet.common.JsonParserUtil;
import com.chuadatten.wallet.kafka.KafkaTopic;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OutBoxSchedule {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final MongoTemplate mongoTemplate;
    private final JsonParserUtil jsonParserUtil;

    private static final int BATCH_SIZE = 100;
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_PROCESSING = "PROCESSING";
    private static final String STATUS_PUBLISHED = "PUBLISHED";
    private static final String STATUS_FAILED = "FAILED";
    private static final String FIELD_STATUS = "status";
    private static final String FIELD_EVENT_TYPE = "event_type";

    @Scheduled(initialDelay = 5000, fixedDelay = 1000)
    public void payProcessingEvent() {
        pollAndPublish(KafkaTopic.PAYMENT_PROCECSSING);
    }

    @Scheduled(initialDelay = 5000, fixedDelay = 1000)
    public void payUrlEvent() {
        pollAndPublish(KafkaTopic.PAYMENT_URL_SUCCESS);
    }

    @Scheduled(initialDelay = 5000, fixedDelay = 1000)
    public void paymentSuccessEvent() {
        pollAndPublish(KafkaTopic.PAYMENT_SUCCESS);
    }

    @Scheduled(initialDelay = 5000, fixedDelay = 1000)
    public void paymentCancelSuccessEvent() {
        pollAndPublish(KafkaTopic.PAYMENT_CANCEL_SUCCESS);
    }
    @Scheduled(initialDelay = 5000, fixedDelay = 1000)
    public void paymentCancelFailedEvent() {
        pollAndPublish(KafkaTopic.PAYMENT_CANCEL_FAILED);
    }

    private Object parserObject(KafkaTopic topic, String payload) {
        switch (topic) {
            case ORDER_CREATED:
                return jsonParserUtil.fromJson(payload, com.chuadatten.event.OrderCreatedEvent.class);
            case PRODUCT_RESERVATION_SUCCESS:
                return jsonParserUtil.fromJson(payload, com.chuadatten.event.ProductReservationSuccessEvent.class);
            case PRODUCT_RESERVATION_FAILED:
                return jsonParserUtil.fromJson(payload, com.chuadatten.event.ProductReservationFailedEvent.class);
            case ORDER_COMFIRM_DATA:
                return jsonParserUtil.fromJson(payload, com.chuadatten.event.OrderComfirmData.class);
            case PAYMENT_PROCECSSING:
                return jsonParserUtil.fromJson(payload, com.chuadatten.event.PaymentProcessEvent.class);
            case CHECKING_ORDER_RETURN:
                return jsonParserUtil.fromJson(payload, com.chuadatten.event.OrderCheckingStatusEvent.class);
            case PAYMENT_URL_SUCCESS:
                return jsonParserUtil.fromJson(payload, com.chuadatten.event.PayUrlEvent.class);
            case PAYMENT_SUCCESS:
                return jsonParserUtil.fromJson(payload, com.chuadatten.event.PaymentSuccessedEvent.class);
            case PAYMENT_CANCEL_SUCCESS:
                return jsonParserUtil.fromJson(payload, com.chuadatten.event.PaymentCancelEvent.class);
            case PAYMENT_CANCEL_FAILED:
                return jsonParserUtil.fromJson(payload, com.chuadatten.event.PaymentCancelEvent.class);
            default:
                return jsonParserUtil.fromJson(payload, Object.class);
        }
    }

    /**
     * Poll và publish event sang Kafka
     */
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

    /**
     * Lock batch bằng findAndModify (tránh nhiều poller đụng cùng record)
     */
    private List<OutboxEvent> lockNextBatch(int size, String eventType) {
        List<OutboxEvent> locked = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            OutboxEvent e = mongoTemplate.findAndModify(
                    new Query(Criteria.where(FIELD_STATUS).is(STATUS_PENDING).and(FIELD_EVENT_TYPE).is(eventType))
                            .with(org.springframework.data.domain.Sort.by("created_at").ascending()),
                    new Update().set(FIELD_STATUS, STATUS_PROCESSING).set("locked_at", Instant.now()),
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
                new Update().set(FIELD_STATUS, STATUS_PUBLISHED).set("publishedAt", Instant.now()),
                OutboxEvent.class);
    }

    private void markFailed(OutboxEvent event, Throwable error) {
        mongoTemplate.updateFirst(
                new Query(Criteria.where("_id").is(event.getId())),
                new Update().set(FIELD_STATUS, STATUS_FAILED).inc("attempts", 1).set("error", error.getMessage()),
                OutboxEvent.class);
    }

}
