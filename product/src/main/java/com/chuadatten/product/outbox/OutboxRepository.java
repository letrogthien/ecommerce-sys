package com.chuadatten.product.outbox;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface OutboxRepository extends MongoRepository<OutboxEvent, String> {

}
