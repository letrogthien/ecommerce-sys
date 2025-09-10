package com.chuadatten.transaction.kafka;

import java.util.UUID;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.chuadatten.event.OrderCheckingStatusEvent;
import com.chuadatten.event.OrderComfirmData;
import com.chuadatten.event.OrderSuccess;
import com.chuadatten.event.PaymentProcessEvent;
import com.chuadatten.event.ProductReservationFailedEvent;
import com.chuadatten.event.ProductReservationSuccessEvent;
import com.chuadatten.transaction.common.JsonParserUtil;
import com.chuadatten.transaction.common.Status;
import com.chuadatten.transaction.entity.Order;
import com.chuadatten.transaction.exceptions.CustomException;
import com.chuadatten.transaction.exceptions.ErrorCode;
import com.chuadatten.transaction.outbox.OutboxEvent;
import com.chuadatten.transaction.outbox.OutboxRepository;
import com.chuadatten.transaction.repository.OrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventListener {
    private final OrderRepository orderRepository;
    private final OutboxRepository outboxRepository;
    private final JsonParserUtil jsonParserUtil;

    @KafkaListener(topics = "product.reservation.failed", groupId = "transaction-group")
    public void listenProductReservationFailed(ProductReservationFailedEvent event) {
        UUID orderId = UUID.fromString(event.getOrderId());
        orderRepository.findById(orderId).ifPresent(order -> {
            order.setStatus(Status.CANCELLED);
            orderRepository.save(order);
        });

    }

    @KafkaListener(topics = "product.reservation.succeeded", groupId = "transaction-group")
    public void listenProductReservationSucceeded(ProductReservationSuccessEvent event) {
        UUID orderId = UUID.fromString(event.getOrderId());
        orderRepository.findById(orderId).ifPresent(order -> {
            order.setTotalAmount(event.getSubtotal());
            order.setStatus(Status.READY_PAY);
            orderRepository.save(order);
        });

        OrderComfirmData orderComfirmData = OrderComfirmData.builder()
                .orderId(event.getOrderId())
                .totalAmount(event.getSubtotal())
                .idempotencyKey(orderId.toString().concat("-").concat("order"))
                .build();
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .eventType(KafkaTopic.ORDER_COMFIRM_DATA.name())
                .aggregateId(orderId.toString())
                .aggregateType(orderRepository.getClass().getName())
                .payload(jsonParserUtil.toJson(orderComfirmData))
                .status("PENDING")
                .build();

        outboxRepository.save(outboxEvent);
    }

    @KafkaListener(topics = "payment.success", groupId = "transaction-group")
    public void listenPaymentSuccess(com.chuadatten.event.PaymentSuccessedEvent event) {
        UUID orderId = UUID.fromString(event.getOrderId());
        orderRepository.findById(orderId).ifPresent(order -> {
            order.setStatus(Status.PAID);
            order.setPaymentStatus(Status.SUCCESS);
            orderRepository.save(order);
            
            // Create OrderSuccess event with product details
            OrderSuccess orderSuccess = OrderSuccess.builder()
                    .orderId(order.getId().toString())
                    .sellerId(order.getSellerId().toString())
                    .finalAmount(order.getTotalAmount())
                    .currency(order.getCurrency())
                    .products(order.getItems().stream()
                            .map(item -> OrderSuccess.ProductQuantityUpdate.builder()
                                    .productId(item.getProductId())
                                    .productVariantId(item.getProductVariantId())
                                    .quantity(item.getQuantity())
                                    .pricePerUnit(item.getUnitPrice())
                                    .subtotal(item.getSubtotal())
                                    .build())
                            .toList())
                    .build();


            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .eventType(KafkaTopic.ORDER_SUCCESS.name())
                    .aggregateId(orderId.toString())
                    .aggregateType(orderRepository.getClass().getName())
                    .payload(jsonParserUtil.toJson(orderSuccess))
                    .status("PENDING")
                    .build();

            outboxRepository.save(outboxEvent);
        });
    }

    @KafkaListener(topics = "payment.payment.processing", groupId = "transaction-group")
    public void listenPaymentProcessing(PaymentProcessEvent event) {
        UUID orderId = UUID.fromString(event.getOrderId());
        Order order = orderRepository.findById(orderId).orElseThrow(()-> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        if (order.getStatus() != Status.READY_PAY){
            handleEventPayment(event, "FAILED");
        } else {
            order.setStatus(Status.PAYING);
            order.setPaymentStatus(Status.PROCESSING);
            orderRepository.save(order);
            handleEventPayment(event, "OK");
        }
    }


    private void handleEventPayment(PaymentProcessEvent event, String status){
        OrderCheckingStatusEvent orderCheckingStatusEvent = OrderCheckingStatusEvent.builder()
                .orderId(event.getOrderId())
                .paymentId(event.getPaymentId())
                .status(status)
                .ip(event.getIp())
                .paymentMethod(event.getPaymentMethod())
                .build();
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .eventType(KafkaTopic.CHECKING_ORDER_RETURN.name())
                .aggregateId(event.getOrderId())
                .aggregateType(orderRepository.getClass().getName())
                .payload(jsonParserUtil.toJson(orderCheckingStatusEvent))
                .status("PENDING")
                .build();
        outboxRepository.save(outboxEvent);
    }

}
