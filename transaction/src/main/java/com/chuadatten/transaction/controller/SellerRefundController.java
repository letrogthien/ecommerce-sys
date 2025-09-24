package com.chuadatten.transaction.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.chuadatten.transaction.anotation.JwtClaims;
import com.chuadatten.transaction.common.Status;
import com.chuadatten.transaction.dto.OrderRefundDto;
import com.chuadatten.transaction.request.RefundApproveRequest;
import com.chuadatten.transaction.request.RefundProcessPaymentRequest;
import com.chuadatten.transaction.request.RefundRejectRequest;
import com.chuadatten.transaction.request.RefundStatusUpdateRequest;
import com.chuadatten.transaction.responses.ApiResponse;
import com.chuadatten.transaction.service.SellerRefundService;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/transaction-service/refunds/seller")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Seller Refund Management", description = "APIs for seller refund management")
public class SellerRefundController {

    private final SellerRefundService sellerRefundService;

    /**
     * Get refunds for seller to process
     * Seller views refunds that require their attention
     */
    @GetMapping("/{sellerId}")
    @PreAuthorize("hasRole('ROLE_SELLER') or hasRole('ROLE_ADMIN')")
    public ApiResponse<Page<OrderRefundDto>> getSellerRefunds(
            @PathVariable UUID sellerId,
            @RequestParam(required = false) Status status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit,
            @Parameter(hidden = true) @JwtClaims("id") UUID currentUserId) {
        
        // Validate seller can only access their own refunds (unless admin)
        // This validation could be enhanced with role checking
        
        return sellerRefundService.getSellerRefunds(sellerId, status, page, limit);
    }

    /**
     * Seller approve refund request
     * Seller approves a refund request from buyer
     */
    @PutMapping("/{refundId}/approve")
    @PreAuthorize("hasRole('ROLE_SELLER') or hasRole('ROLE_ADMIN')")
    public ApiResponse<OrderRefundDto> approveRefund(
            @PathVariable UUID refundId,
            @Valid @RequestBody RefundApproveRequest request,
            @Parameter(hidden = true) @JwtClaims("id") UUID currentUserId) {
        
        return sellerRefundService.approveRefund(refundId, request);
    }

    /**
     * Seller reject refund request
     * Seller rejects a refund request with reason
     */
    @PutMapping("/{refundId}/reject")
    @PreAuthorize("hasRole('ROLE_SELLER') or hasRole('ROLE_ADMIN')")
    public ApiResponse<OrderRefundDto> rejectRefund(
            @PathVariable UUID refundId,
            @Valid @RequestBody RefundRejectRequest request,
            @Parameter(hidden = true) @JwtClaims("id") UUID currentUserId) {
        
        return sellerRefundService.rejectRefund(refundId, request);
    }

    /**
     * Update refund status (admin/system)
     * Admin or system updates refund status
     */
    @PutMapping("/{refundId}/status")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ApiResponse<OrderRefundDto> updateRefundStatus(
            @PathVariable UUID refundId,
            @Valid @RequestBody RefundStatusUpdateRequest request,
            @Parameter(hidden = true) @JwtClaims("id") UUID currentUserId) {
        
        return sellerRefundService.updateRefundStatus(refundId, request);
    }

    /**
     * Process refund payment
     * Integrate with payment gateway to process refund
     */
    @PostMapping("/{refundId}/process-payment")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ApiResponse<OrderRefundDto> processRefundPayment(
            @PathVariable UUID refundId,
            @Valid @RequestBody RefundProcessPaymentRequest request,
            @Parameter(hidden = true) @JwtClaims("id") UUID currentUserId) {
        
        return sellerRefundService.processRefundPayment(refundId, request);
    }
}