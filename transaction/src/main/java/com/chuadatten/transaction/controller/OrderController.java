package com.chuadatten.transaction.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.chuadatten.transaction.anotation.JwtClaims;
import com.chuadatten.transaction.dto.OrderDto;
import com.chuadatten.transaction.request.OrderCreateRq;
import com.chuadatten.transaction.request.OrderProofCreateRq;
import com.chuadatten.transaction.responses.ApiResponse;
import com.chuadatten.transaction.service.OrderService;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/transaction-service/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    /**
     * Create a new order
     * Buyer creates a new order with product list, seller, total amount, and currency.
     */
    @PostMapping
    public ApiResponse<OrderDto> createOrder(
            @Parameter(hidden = true) @JwtClaims("id") UUID buyerId,
            @RequestBody OrderCreateRq orderCreateRq) {
        return orderService.createOrder(orderCreateRq, buyerId);
    }

    /**
     * View order details
     * Buyer or Seller views details of an order, including items, status, and logs.
     */
    @GetMapping("/{orderId}")
    public ApiResponse<OrderDto> getOrderById(
            @Parameter(hidden = true)@JwtClaims("id") UUID userId,
            @PathVariable UUID orderId) {
        return orderService.getOrderById(orderId, userId);
    }

    /**
     * Get buyer's order list
     * Get all orders of a buyer, with filtering by status and pagination/sorting.
     */
    @GetMapping("/buyer")
    public ApiResponse<Page<OrderDto>> getOrdersByBuyer(
            @Parameter(hidden = true) @JwtClaims("id") UUID buyerId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {
        return orderService.getOrdersByBuyer(buyerId, status, page, limit);
    }

    /**
     * Get seller's order list
     * Get all orders of a seller, with filtering by status and pagination/sorting.
     */
    @GetMapping("/seller")
    public ApiResponse<Page<OrderDto>> getOrdersBySeller(
            @Parameter(hidden = true) @JwtClaims("id") UUID sellerId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {
        return orderService.getOrdersBySeller(sellerId, status, page, limit);
    }

    /**
     * Cancel order
     * Buyer can cancel the order if it's not in COMPLETED, TRADE_CLOSED, or REFUNDING status.
     */
    @PutMapping("/{orderId}/cancel")
    public ApiResponse<OrderDto> cancelOrder(
            @Parameter(hidden = true) @JwtClaims("id") UUID buyerId,
            @PathVariable UUID orderId) {
        return orderService.cancelOrder(orderId, buyerId);
    }

    /**
     * Upload delivery proof
     * Seller uploads images, videos, or notes as evidence for dispute/audit.
     */
    @PostMapping(value = "/{orderId}/proof", consumes = {"multipart/form-data"})
    public ApiResponse<OrderDto> uploadDeliveryProof(
            @Parameter(hidden = true) @JwtClaims("id") UUID sellerId,
            @PathVariable UUID orderId,
            @RequestPart("proofData") OrderProofCreateRq proofCreateRq,
            @RequestPart("file") MultipartFile file) {
        return orderService.uploadDeliveryProof(orderId, proofCreateRq, file, sellerId);
    }
}