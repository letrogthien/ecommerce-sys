package com.chuadatten.transaction.service;

import java.util.UUID;

import org.springframework.data.domain.Page;

import com.chuadatten.transaction.common.Status;
import com.chuadatten.transaction.dto.OrderRefundDto;
import com.chuadatten.transaction.request.RefundApproveRequest;
import com.chuadatten.transaction.request.RefundProcessPaymentRequest;
import com.chuadatten.transaction.request.RefundRejectRequest;
import com.chuadatten.transaction.request.RefundStatusUpdateRequest;
import com.chuadatten.transaction.responses.ApiResponse;

/**
 * Service interface for seller refund management operations.
 */
public interface SellerRefundService {

    /**
     * Get refunds for seller to process
     * Seller views refunds that require their attention
     *
     * @param sellerId the ID of the seller
     * @param status optional status filter (PENDING, APPROVED, REJECTED, etc.)
     * @param page the page number (starting from 0)
     * @param limit the maximum number of items to return
     * @return ApiResponse containing a page of refunds for the seller
     */
    ApiResponse<Page<OrderRefundDto>> getSellerRefunds(UUID sellerId, Status status, int page, int limit);

    /**
     * Seller approve refund request
     * Seller approves a refund request from buyer
     *
     * @param refundId the ID of the refund to approve
     * @param request the approval request containing seller ID and optional note
     * @return ApiResponse containing the updated refund
     */
    ApiResponse<OrderRefundDto> approveRefund(UUID refundId, RefundApproveRequest request);

    /**
     * Seller reject refund request
     * Seller rejects a refund request with reason
     *
     * @param refundId the ID of the refund to reject
     * @param request the rejection request containing seller ID and reason
     * @return ApiResponse containing the updated refund
     */
    ApiResponse<OrderRefundDto> rejectRefund(UUID refundId, RefundRejectRequest request);

    /**
     * Update refund status (admin/system)
     * Admin or system updates refund status
     *
     * @param refundId the ID of the refund to update
     * @param request the status update request
     * @return ApiResponse containing the updated refund
     */
    ApiResponse<OrderRefundDto> updateRefundStatus(UUID refundId, RefundStatusUpdateRequest request);

    /**
     * Process refund payment
     * Integrate with payment gateway to process refund
     *
     * @param refundId the ID of the refund to process payment for
     * @param request the payment processing request
     * @return ApiResponse containing the updated refund
     */
    ApiResponse<OrderRefundDto> processRefundPayment(UUID refundId, RefundProcessPaymentRequest request);
}