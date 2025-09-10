package com.chuadatten.transaction.outbox;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface OutboxRepository extends MongoRepository<OutboxEvent, String> {

}
