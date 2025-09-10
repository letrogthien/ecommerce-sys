package com.chuadatten.transaction.kafka;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageErrorRepository extends MongoRepository<SendMessageError, String>{
    
}
