package com.chuadatten.wallet.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.chuadatten.event.PaymentProcessEvent;



@Service
@RequiredArgsConstructor
public class EventProducer {
    private final SendEvent s;


    public void payProcessingEvent(PaymentProcessEvent event) {
        s.sendEvent(KafkaTopic.PAYMENT_PROCECSSING.getTopicName(), event);
    }


}
