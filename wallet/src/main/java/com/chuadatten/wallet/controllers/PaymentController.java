package com.chuadatten.wallet.controllers;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.chuadatten.wallet.anotation.JwtClaims;
import com.chuadatten.wallet.common.PaymentMethod;
import com.chuadatten.wallet.dto.PaymentAttemptDto;
import com.chuadatten.wallet.dto.PaymentDto;
import com.chuadatten.wallet.responses.ApiResponse;
import com.chuadatten.wallet.service.PaymentService;
import com.chuadatten.wallet.vnpay.VnpayReturnDto;
import com.fasterxml.jackson.core.JsonProcessingException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/wallet-service/payments")
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/{paymentId}/process")
    public ApiResponse<String> processPayment(
            @PathVariable UUID paymentId,
            @RequestParam PaymentMethod paymentMethod,
            @Parameter(hidden = true) @JwtClaims("id") UUID userId,
            HttpServletRequest request)
            throws InvalidKeyException, NoSuchAlgorithmException, JsonProcessingException {
        String clientIp = getClientIpAddress(request);
        return paymentService.payment(paymentId, clientIp, userId, paymentMethod);
    }

    @GetMapping("/{paymentId}")
    public ApiResponse<PaymentDto> getPaymentById(@PathVariable UUID paymentId) {
        return paymentService.getPaymentById(paymentId);
    }

    @GetMapping("/{paymentId}/status")
    public ApiResponse<PaymentDto> getPaymentStatus(@PathVariable UUID paymentId) {
        return paymentService.getPaymentStatus(paymentId);
    }

    @GetMapping("/order/{orderId}")
    public ApiResponse<PaymentDto> getPaymentByOrderId(@PathVariable UUID orderId) {
        return paymentService.getPaymentByOrderId(orderId);
    }

    @GetMapping("/me")
    public ApiResponse<Page<PaymentDto>> getUserPayments(
            @Parameter(hidden = true) @JwtClaims("id") UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return paymentService.getUserPayments(userId, page, size);
    }

    @PostMapping("/{paymentId}/retry")
    public ApiResponse<String> retryPayment(@PathVariable UUID paymentId) {
        return paymentService.retryPayment(paymentId);
    }

    @PutMapping("/{paymentId}/cancel")
    @Operation(summary = "Cancel payment", description = "Cancel a pending payment")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelPayment(@PathVariable UUID paymentId) {
        paymentService.cancelPayment(paymentId);
    }

    @PostMapping("/{paymentId}/refund")
    @Operation(summary = "Refund payment", description = "Process payment refund")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void refundPayment(
            @PathVariable UUID paymentId,
            @RequestHeader("Seller-Id") UUID sellerId,
            HttpServletRequest request)
            throws JsonProcessingException, InvalidKeyException, NumberFormatException, NoSuchAlgorithmException {
        String clientIp = getClientIpAddress(request);
        paymentService.refundPayment(paymentId, sellerId, clientIp);
    }

    @GetMapping("/{paymentId}/attempts")
    @Operation(summary = "Get payment attempts", description = "Get payment attempt history")
    public ApiResponse<PaymentAttemptDto> getPaymentAttempts(@PathVariable UUID paymentId) {
        return paymentService.getPaymentAttempts(paymentId);
    }

    // VNPay callback endpoints
    @GetMapping("/vnpay_return")
    @Operation(summary = "VNPay return callback", description = "Handle VNPay return callback")
    public ApiResponse<VnpayReturnDto> handleVnpayReturnDeposit(@ModelAttribute VnpayReturnDto vnpayReturnDto) {
        return ApiResponse.<VnpayReturnDto>builder()
                .data(vnpayReturnDto)
                .message("ok")
                .build();
    }

    @GetMapping("/IPN")
    @Operation(summary = "VNPay IPN callback", description = "Handle VNPay IPN (Instant Payment Notification) callback")
    public ApiResponse<String> handleVnpayIpn(@ModelAttribute VnpayReturnDto vnpayReturnDto)
            throws InvalidKeyException, JsonProcessingException, NoSuchAlgorithmException {
        paymentService.handleProviderCallback(vnpayReturnDto);
        return ApiResponse.<String>builder()
                .data("ok")
                .message("ok")
                .build();
    }

    // Helper method to get client IP address
    private String getClientIpAddress(HttpServletRequest request) {
   
        return request.getRemoteAddr();
      
    }

}
