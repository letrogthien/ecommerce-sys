package com.chuadatten.transaction.outbox;

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

import com.chuadatten.event.OrderCheckingStatusEvent;
import com.chuadatten.event.OrderComfirmData;
import com.chuadatten.event.OrderCreatedEvent;
import com.chuadatten.event.OrderSuccess;
import com.chuadatten.transaction.common.JsonParserUtil;
import com.chuadatten.transaction.kafka.KafkaTopic;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OutBoxSchedule {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final MongoTemplate mongoTemplate;
    private final JsonParserUtil jsonParserUtil;

    private static final int BATCH_SIZE = 100;
    private static final String STATUS_FIELD = "status";
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_PROCESSING = "PROCESSING";
    private static final String STATUS_PUBLISHED = "PUBLISHED";
    private static final String STATUS_FAILED = "FAILED";

    @Scheduled(initialDelay = 5000, fixedDelay = 1000)
    public void orderCreated() {
        pollAndPublish(KafkaTopic.ORDER_CREATED);
    }

    @Scheduled(initialDelay = 5000, fixedDelay = 1000)
    public void orderComfirmData() {
        pollAndPublish(KafkaTopic.ORDER_COMFIRM_DATA);
    }


    @Scheduled(initialDelay = 5000, fixedDelay = 1000)
    public void checkingOrderReturn() {
        pollAndPublish(KafkaTopic.CHECKING_ORDER_RETURN);
    }

    @Scheduled(initialDelay = 5000, fixedDelay = 1000)
    public void orderSuccess() {
        pollAndPublish(KafkaTopic.ORDER_SUCCESS);
    }


    private Object parserObject(KafkaTopic topic, String payload) {
        switch (topic) {
            case ORDER_CREATED:
                return jsonParserUtil.fromJson(payload, OrderCreatedEvent.class);
            case ORDER_COMFIRM_DATA:
                return jsonParserUtil.fromJson(payload, OrderComfirmData.class);
            case CHECKING_ORDER_RETURN:
                return jsonParserUtil.fromJson(payload, OrderCheckingStatusEvent.class);
            case ORDER_SUCCESS:
                return jsonParserUtil.fromJson(payload, OrderSuccess.class);
            default:
                return jsonParserUtil.fromJson(payload, Object.class);
        }
    }

    /**
     * Poll và publish event order created sang Kafka
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
                    new Query(Criteria.where(STATUS_FIELD).is(STATUS_PENDING).and("event_type").is(eventType))
                            .with(org.springframework.data.domain.Sort.by("created_at").ascending()),
                    new Update().set(STATUS_FIELD, STATUS_PROCESSING).set("locked_at", Instant.now()),
                    org.springframework.data.mongodb.core.FindAndModifyOptions.options().returnNew(true),
                    OutboxEvent.class);
            if (e == null) {
                break;
            }
            locked.add(e);
        }
        return locked;
    }

    private void markPublished(OutboxEvent event) {
        mongoTemplate.updateFirst(
                new Query(Criteria.where("_id").is(event.getId())),
                new Update().set(STATUS_FIELD, STATUS_PUBLISHED).set("published_at", Instant.now()),
                OutboxEvent.class);
    }

    private void markFailed(OutboxEvent event, Throwable error) {
        mongoTemplate.updateFirst(
                new Query(Criteria.where("_id").is(event.getId())),
                new Update().set(STATUS_FIELD, STATUS_FAILED).inc("attempts", 1),
                OutboxEvent.class);
    }


}