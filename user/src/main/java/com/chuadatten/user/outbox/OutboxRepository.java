package com.chuadatten.user.outbox;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface OutboxRepository extends MongoRepository<OutboxEvent, String> {
    List<OutboxEvent> findTop500ByStatusOrderByCreatedAtAsc(String status);
    List<OutboxEvent> findTop500ByStatusAndAggregateTypeOrderByCreatedAtAsc(String status, String aggregateType);
}
