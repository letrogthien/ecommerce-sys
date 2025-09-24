package com.chuadatten.product.kafka;

import java.math.BigDecimal;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.chuadatten.event.OrderCancel;
import com.chuadatten.event.OrderCreatedEvent;
import com.chuadatten.event.OrderCreatedEvent.OrderItemEvent;
import com.chuadatten.event.OrderSuccess;
import com.chuadatten.event.ProductReservationFailedEvent;
import com.chuadatten.event.ProductReservationSuccessEvent;
import com.chuadatten.product.common.JsonParserUtil;
import com.chuadatten.product.entity.ProductVariant;
import com.chuadatten.product.exceptions.CustomException;
import com.chuadatten.product.exceptions.ErrorCode;
import com.chuadatten.product.outbox.OutboxEvent;
import com.chuadatten.product.outbox.OutboxRepository;
import com.chuadatten.product.repository.ProductVariantRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventListener {
    private final ProductVariantRepository productVariantRepository;
    private final OutboxRepository outboxEventRepository;
    private final JsonParserUtil jsonParserUtil;



    @KafkaListener(topics = "transaction.order.cancel", groupId = "product-group")
    public void listenOrderCancelEvent(OrderCancel event) {
        handleReleaseReservedStock(event);
    }
    private void handleReleaseReservedStock(OrderCancel event) {
        if (event.getItems().size() > 1) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
        com.chuadatten.event.OrderCancel.OrderItemEvent item = event.getItems().get(0);

        productVariantRepository.findById(item.getProductVariantId()).ifPresent(variant -> {
            variant.setAvailableQty(variant.getAvailableQty() + item.getQuantity());
            variant.setReservedQty(variant.getReservedQty() - item.getQuantity());
            productVariantRepository.save(variant);
        });
    }

    @KafkaListener(topics = "transaction.order.created", groupId = "product-group")
    public void listenOrderCreatedEvent(OrderCreatedEvent event) {
        if (event.getItems().size() > 1) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
        OrderItemEvent item = event.getItems().get(0);

        productVariantRepository.findById(item.getProductVariantId()).ifPresent(variant -> {
            if (variant.getAvailableQty() < item.getQuantity()) {
                ProductReservationFailedEvent failedEvent = ProductReservationFailedEvent.builder()
                        .orderId(event.getOrderId())
                        .build();
                OutboxEvent outboxEvent = OutboxEvent.builder()
                        .eventType(KafkaTopic.PRODUCT_RESERVATION_FAILED.name())
                        .aggregateId(item.getProductVariantId())
                        .aggregateType(ProductVariant.class.getName())
                        .payload(jsonParserUtil.toJson(failedEvent))
                        .status("PENDING")
                        .build();
                outboxEventRepository.save(outboxEvent);
                throw new CustomException(ErrorCode.INSUFFICIENT_STOCK);
            }
            variant.setAvailableQty(variant.getAvailableQty() - item.getQuantity());
            variant.setReservedQty(variant.getReservedQty() + item.getQuantity());
            productVariantRepository.save(variant);

            ProductReservationSuccessEvent successEvent = ProductReservationSuccessEvent.builder()
                    .orderId(event.getOrderId())
                    .subtotal(BigDecimal.valueOf(variant.getPrice().longValue() * item.getQuantity()))
                    .build();
            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .eventType(KafkaTopic.PRODUCT_RESERVATION_SUCCESS.name())
                    .aggregateId(item.getProductVariantId())
                    .aggregateType(ProductVariant.class.getName())
                    .payload(jsonParserUtil.toJson(successEvent))
                    .status("PENDING")
                    .build();
            outboxEventRepository.save(outboxEvent);
        });

    }

    @KafkaListener(topics = "transaction.order.success", groupId = "transaction-group")
    public void listenOrderSuccess(OrderSuccess event) {
        // Process each product in the successful order
        event.getProducts().forEach(product -> 
            productVariantRepository.findById(product.getProductVariantId())
                .ifPresent(variant -> {

                    variant.setReservedQty(variant.getReservedQty() - product.getQuantity());
                    variant.setSoldQty(variant.getSoldQty() + product.getQuantity());
                    variant.setAvailableQty(variant.getAvailableQty() - product.getQuantity());
        
                    productVariantRepository.save(variant);
                })
        );
    }

    @KafkaListener(topics = "transaction.clean.up.order", groupId = "transaction-group")
    public void listenCleanUpOrderEvent(OrderCancel event) {
        handleReleaseReservedStock(event);
    }
}