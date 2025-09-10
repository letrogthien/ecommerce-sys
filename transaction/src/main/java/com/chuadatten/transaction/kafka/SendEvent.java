package com.chuadatten.transaction.kafka;


import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class SendEvent {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final MessageErrorRepository messageErrorRepository;

    @Async
    public void sendEvent(String topic, Object event) {
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, event);
        future.whenComplete((SendResult<String, Object> result, Throwable ex) -> {
            if (ex != null) {
                String message = (event != null) ? event.toString() : "null";
                messageErrorRepository.save(SendMessageError.builder()
                        .message(message)
                        .topic(topic)
                        .build());
            }
        });
    }
}
