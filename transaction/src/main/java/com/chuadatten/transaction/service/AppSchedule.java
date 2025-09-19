package com.chuadatten.transaction.service;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.chuadatten.event.OrderCancel;
import com.chuadatten.transaction.common.JsonParserUtil;
import com.chuadatten.transaction.entity.Order;
import com.chuadatten.transaction.entity.OrderItem;
import com.chuadatten.transaction.kafka.KafkaTopic;
import com.chuadatten.transaction.outbox.OutboxEvent;
import com.chuadatten.transaction.outbox.OutboxRepository;
import com.chuadatten.transaction.repository.OrderItemRepository;
import com.chuadatten.transaction.repository.OrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppSchedule {
    private final OrderRepository orderRepository;
    private final JsonParserUtil jsonParserUtil;
    private final OutboxRepository outboxRepository;


    @Scheduled(initialDelay = 5000, fixedDelay = 1000)
    public void cleanUpTransactions() {
        cleanUpOldTransactions();
    }


    private void cleanUpOldTransactions() {
        orderRepository.findOrdersToCancel().forEach(order -> {

            OrderCancel orderCancel = OrderCancel.builder()
                    .items(
                        order.getItems().stream().map(item -> OrderCancel.OrderItemEvent.builder()
                                .productId(item.getProductId())
                                .productVariantId(item.getProductVariantId())
                                .quantity(item.getQuantity())
                            
                                .build()
                        ).toList()
                    )
                    .orderId(order.getId().toString())
                    .build();
            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .eventType(KafkaTopic.CLEAN_UP_ORDER.name())
                    .aggregateId(order.getId().toString())
                    .payload(jsonParserUtil.toJson(orderCancel))
                    .status("PENDING")
                    .build();
            outboxRepository.save(outboxEvent);

            //set status = cancelled
            order.setStatus(com.chuadatten.transaction.common.Status.CANCELLED);
            orderRepository.save(order);
        });
   
    }
    
}
