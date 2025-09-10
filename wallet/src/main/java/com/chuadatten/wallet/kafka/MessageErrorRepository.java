package com.chuadatten.wallet.kafka;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface MessageErrorRepository extends MongoRepository<SendMessageError, String>{
    
}
