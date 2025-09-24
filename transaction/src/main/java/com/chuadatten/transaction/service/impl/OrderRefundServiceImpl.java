package com.chuadatten.transaction.service.impl;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.chuadatten.transaction.common.Status;
import com.chuadatten.transaction.dto.OrderRefundDto;
import com.chuadatten.transaction.entity.Order;
import com.chuadatten.transaction.entity.OrderRefund;
import com.chuadatten.transaction.exceptions.CustomException;
import com.chuadatten.transaction.exceptions.ErrorCode;
import com.chuadatten.transaction.mapper.TransactionMapper;
import com.chuadatten.transaction.repository.OrderRefundRepository;
import com.chuadatten.transaction.repository.OrderRepository;
import com.chuadatten.transaction.request.OrderRefundCreateRq;
import com.chuadatten.transaction.responses.ApiResponse;
import com.chuadatten.transaction.service.OrderRefundService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderRefundServiceImpl implements OrderRefundService {
    private final OrderRefundRepository orderRefundRepository;
    private final OrderRepository orderRepository;
    private final TransactionMapper transactionMapper;

    @Override
    public ApiResponse<OrderRefundDto> requestRefund(OrderRefundCreateRq refundCreateRq, UUID buyerId) {
        Order order = orderRepository.findById(refundCreateRq.getOrderId()).orElseThrow(
                () -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
        if (!order.getBuyerId().equals(buyerId)) {

            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        OrderRefund orderRefund = OrderRefund.builder()
                .order(order)
                .reason(refundCreateRq.getReason())
                .requestBy(buyerId)
                .build();

        orderRefundRepository.save(orderRefund);
        return ApiResponse.<OrderRefundDto>builder()
                .data(transactionMapper.toOrderRefundDto(orderRefund))
                .build();

    }

    @Override
    public ApiResponse<OrderRefundDto> getRefundStatus(UUID orderId, UUID buyerId) {
        OrderRefund orderRefund = orderRefundRepository.findByOrderIdAndRequestBy(orderId, buyerId).orElseThrow(
                () -> new CustomException(ErrorCode.REFUND_FOR_ORDER_NOT_FOUND));
        return ApiResponse.<OrderRefundDto>builder()
                .data(transactionMapper.toOrderRefundDto(orderRefund))
                .build();
    }

    @Override
    public ApiResponse<Page<OrderRefundDto>> getAllRefunds(Status status, UUID buyerId, int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<OrderRefundDto> orderRefunds = orderRefundRepository.findByRequestBy(buyerId, pageable)
                .map(
                        transactionMapper::toOrderRefundDto);

        return ApiResponse.<Page<OrderRefundDto>>builder()
                .data(orderRefunds)
                .build();

    }


}
