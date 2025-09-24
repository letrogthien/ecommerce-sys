package com.chuadatten.transaction.service.impl;

import java.util.ArrayList;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.chuadatten.event.OrderCancel;
import com.chuadatten.event.OrderCreatedEvent;
import com.chuadatten.transaction.common.JsonParserUtil;
import com.chuadatten.transaction.common.ProofType;
import com.chuadatten.transaction.common.Status;
import com.chuadatten.transaction.dto.OrderDto;
import com.chuadatten.transaction.entity.Order;
import com.chuadatten.transaction.entity.OrderItem;
import com.chuadatten.transaction.entity.OrderProof;
import com.chuadatten.transaction.exceptions.CustomException;
import com.chuadatten.transaction.exceptions.ErrorCode;
import com.chuadatten.transaction.file.FileStorageService;
import com.chuadatten.transaction.kafka.KafkaTopic;
import com.chuadatten.transaction.mapper.TransactionMapper;
import com.chuadatten.transaction.outbox.OutboxEvent;
import com.chuadatten.transaction.outbox.OutboxRepository;
import com.chuadatten.transaction.repository.OrderItemRepository;
import com.chuadatten.transaction.repository.OrderProofRepository;
import com.chuadatten.transaction.repository.OrderRepository;
import com.chuadatten.transaction.request.OrderCreateRq;
import com.chuadatten.transaction.request.OrderProofCreateRq;
import com.chuadatten.transaction.responses.ApiResponse;
import com.chuadatten.transaction.service.OrderService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
        private final OrderRepository orderRepository;
        private final TransactionMapper transactionMapper;
        private final OrderProofRepository orderProofRepository;
        private final FileStorageService fileStorageService;
        private final OrderItemRepository orderItemRepository;
        private final JsonParserUtil jsonParserUtil;
        private final OutboxRepository outboxRepository;

        @Override
        public ApiResponse<OrderDto> createOrder(OrderCreateRq orderCreateRq, UUID buyer) {
                Order order = Order.builder()
                                .sellerId(orderCreateRq.getSellerId())
                                .totalAmount(orderCreateRq.getTotalAmount())
                                .status(Status.PENDING)
                                .currency(orderCreateRq.getCurrency())
                                .buyerId(buyer)
                                .items(new ArrayList<>())
                                .build();
                orderRepository.save(order);
                

                orderCreateRq.getItems().forEach(item -> {
                        OrderItem orderItem = OrderItem.builder()
                                        .order(order)
                                        .productId(item.getProductId())
                                        .quantity(item.getQuantity())
                                        .unitPrice(item.getUnitPrice())
                                        .productVariantId(item.getProductVariantId())
                                        .build();
                        orderItem.setOrder(order);
                        order.getItems().add(orderItem);
                        orderItemRepository.save(orderItem);
                });

                OrderCreatedEvent orderCreatedEvent = OrderCreatedEvent.builder()
                                .orderId(order.getId().toString())
                                .buyerId(order.getBuyerId().toString())
                                .sellerId(order.getSellerId().toString())
                                .totalAmount(order.getTotalAmount())
                                .currency(order.getCurrency())
                                .items(order.getItems().stream().map(item -> OrderCreatedEvent.OrderItemEvent.builder()
                                                .productId(item.getProductId())
                                                .productVariantId(item.getProductVariantId())
                                                .quantity(item.getQuantity())
                                                .unitPrice(item.getUnitPrice())
                                                .build()).toList())
                                .build();

                // save outbox event
                OutboxEvent outboxEvent = OutboxEvent.builder()
                                .eventType(KafkaTopic.ORDER_CREATED.name())
                                .aggregateId(order.getId().toString())
                                .aggregateType(Order.class.getName())
                                .status("PENDING")
                                .payload(jsonParserUtil.toJson(orderCreatedEvent))
                                .build();
        
                
                outboxRepository.save(outboxEvent);
                

                orderRepository.save(order);

                return ApiResponse.<OrderDto>builder()
                                .data(transactionMapper.toOrderDto(order))
                                .build();
        }

        @Override
        public ApiResponse<OrderDto> getOrderById(UUID orderId, UUID userId) {
                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
                if (!order.getBuyerId().equals(userId)) {
                        throw new CustomException(ErrorCode.U_NOT_HAVE_PERMISSION);
                }
                return ApiResponse.<OrderDto>builder()
                                .data(transactionMapper.toOrderDto(order))
                                .build();

        }

        @Override
        public ApiResponse<Page<OrderDto>> getOrdersByBuyer(UUID buyerId, String status, int page, int limit) {
                Pageable pageable = PageRequest.of(page, limit);
                Page<OrderDto> orders = orderRepository.findAllByBuyerId(buyerId, pageable)
                                .map(transactionMapper::toOrderDto);
                return ApiResponse.<Page<OrderDto>>builder()
                                .data(orders)
                                .build();

        }

        @Override
        public ApiResponse<Page<OrderDto>> getOrdersBySeller(UUID sellerId, String status, int page, int limit) {
                Pageable pageable = PageRequest.of(page, limit);
                Page<OrderDto> orders = orderRepository.findAllBySellerId(sellerId, pageable)
                                .map(transactionMapper::toOrderDto);
                return ApiResponse.<Page<OrderDto>>builder()
                                .data(orders)
                                .build();
        }

        @Override
        @Transactional
        public ApiResponse<OrderDto> cancelOrder(UUID orderId, UUID buyerId) {
                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
                if (order.getStatus().equals(Status.PAID) || order.getStatus().equals(Status.COMPLETED)
                                || order.getStatus().equals(Status.PAYING)) {
                        throw new CustomException(ErrorCode.ORDER_CANNOT_CANCEL);
                }
                OrderCancel orderCancel = OrderCancel.builder()
                                .orderId(order.getId().toString())
                                .items(order.getItems().stream().map(item -> OrderCancel.OrderItemEvent.builder()
                                                .productId(item.getProductId())
                                                .productVariantId(item.getProductVariantId())
                                                .quantity(item.getQuantity())
                                                .unitPrice(item.getUnitPrice())
                                                .build()).toList())
                                .build();
                OutboxEvent outboxEvent = OutboxEvent.builder()
                                .eventType(KafkaTopic.ORDER_CANCEL.name())
                                .aggregateId(order.getId().toString())
                                .aggregateType(Order.class.getName())
                                .status("PENDING")
                                .payload(jsonParserUtil.toJson(orderCancel))
                                .build();
                outboxRepository.save(outboxEvent);
                order.setStatus(Status.CANCELLED);
                orderRepository.save(order);
                return ApiResponse.<OrderDto>builder()
                                .data(transactionMapper.toOrderDto(order))
                                .build();
        }

        @Override
        public ApiResponse<OrderDto> uploadDeliveryProof(UUID orderId, OrderProofCreateRq proofCreateRq,
                        MultipartFile file, UUID sellerId) {
                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
                if (!order.getStatus().equals(Status.PAID)) {
                        throw new CustomException(ErrorCode.ORDER_CANNOT_UPLOAD_PROOF);
                }
                String urlString = fileStorageService.storeFile(file, "proof", sellerId.toString());
                OrderProof orderProof = OrderProof.builder()
                                .order(order)
                                .note(proofCreateRq.getNote())
                                .sellerId(sellerId)
                                .url(urlString)
                                .type(ProofType.DELIVERY)
                                .build();
                orderProofRepository.save(orderProof);
                order.setStatus(Status.DELIVERED);
                orderRepository.save(order);
                return ApiResponse.<OrderDto>builder()
                                .data(transactionMapper.toOrderDto(order))
                                .build();

        }

}
