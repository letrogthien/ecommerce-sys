package com.chuadatten.transaction.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;



@Service
@RequiredArgsConstructor
public class EventProducer {
    private final SendEvent s;


    public void orderCreated(Object event) {
        s.sendEvent(KafkaTopic.ORDER_CREATED.getTopicName(), event);
    }



}
