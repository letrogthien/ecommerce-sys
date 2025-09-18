package com.chuadatten.transaction.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.chuadatten.transaction.anotation.JwtClaims;
import com.chuadatten.transaction.common.Status;
import com.chuadatten.transaction.dto.OrderRefundDto;
import com.chuadatten.transaction.request.OrderRefundCreateRq;
import com.chuadatten.transaction.responses.ApiResponse;
import com.chuadatten.transaction.service.OrderRefundService;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/transaction-service/refunds")
@RequiredArgsConstructor
public class OrderRefundController {
    private final OrderRefundService orderRefundService;

    /**
     * Request refund
     * Buyer sends a refund request with reason and amount.
     */
    @PostMapping
    public ApiResponse<OrderRefundDto> requestRefund(
            @Parameter(hidden = true) @JwtClaims("id") UUID buyerId,
            @RequestBody OrderRefundCreateRq refundCreateRq) {
        return orderRefundService.requestRefund(refundCreateRq, buyerId);
    }

    /**
     * View refund status
     * Buyer views the refund progress for an order.
     */
    @GetMapping("/order/{orderId}")
    public ApiResponse<OrderRefundDto> getRefundStatus(
            @Parameter(hidden = true) @JwtClaims("id") UUID buyerId,
            @PathVariable UUID orderId) {
        return orderRefundService.getRefundStatus(orderId, buyerId);
    }

    /**
     * Get all refunds
     * Admin views all refunds, filtered by status, order, buyer.
     */
    @GetMapping
    public ApiResponse<Page<OrderRefundDto>> getAllRefunds(
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) UUID buyerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {
        return orderRefundService.getAllRefunds(status, buyerId, page, limit);
    }
}