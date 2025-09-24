package com.chuadatten.transaction.service.impl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chuadatten.transaction.common.Status;
import com.chuadatten.transaction.dto.OrderRefundDto;
import com.chuadatten.transaction.entity.OrderRefund;
import com.chuadatten.transaction.exceptions.CustomException;
import com.chuadatten.transaction.exceptions.ErrorCode;
import com.chuadatten.transaction.mapper.TransactionMapper;
import com.chuadatten.transaction.repository.OrderRefundRepository;
import com.chuadatten.transaction.request.RefundApproveRequest;
import com.chuadatten.transaction.request.RefundProcessPaymentRequest;
import com.chuadatten.transaction.request.RefundRejectRequest;
import com.chuadatten.transaction.request.RefundStatusUpdateRequest;
import com.chuadatten.transaction.responses.ApiResponse;
import com.chuadatten.transaction.service.SellerRefundService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SellerRefundServiceImpl implements SellerRefundService {

    private final OrderRefundRepository orderRefundRepository;
    private final TransactionMapper transactionMapper;

    @Override
    public ApiResponse<Page<OrderRefundDto>> getSellerRefunds(UUID sellerId, Status status, int page, int limit) {
        // Validate input
        if (sellerId == null) {
            throw new CustomException(ErrorCode.INVALID_PARAMETER);
        }

        Pageable pageable = PageRequest.of(page, limit);
        
        try {
            // Use repository query with JOIN to get refunds for seller's orders
            Page<OrderRefund> refunds = orderRefundRepository.findRefundsBySeller(sellerId, status, pageable);
            Page<OrderRefundDto> refundDtos = refunds.map(transactionMapper::toOrderRefundDto);

            return ApiResponse.<Page<OrderRefundDto>>builder()
                    .data(refundDtos)
                    .message("Seller refunds retrieved successfully")
                    .build();

        } catch (Exception e) {
            log.error("Error retrieving seller refunds for sellerId: {}", sellerId, e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public ApiResponse<OrderRefundDto> approveRefund(UUID refundId, RefundApproveRequest request) {
        // Validate input
        if (refundId == null || request.getSellerId() == null) {
            throw new CustomException(ErrorCode.INVALID_PARAMETER);
        }

        try {
            // Find refund and validate it belongs to seller
            OrderRefund refund = orderRefundRepository.findByIdAndSellerId(refundId, request.getSellerId())
                    .orElseThrow(() -> new CustomException(ErrorCode.REFUND_FOR_ORDER_NOT_FOUND));

            // Validate current status
            if (!Status.PENDING.equals(refund.getStatus())) {
                throw new CustomException(ErrorCode.INVALID_PARAMETER);
            }

            // Update status to APPROVED
            refund.setStatus(Status.APPROVED);
            
            // Append seller note to reason if provided
            if (request.getNote() != null && !request.getNote().trim().isEmpty()) {
                String updatedReason = refund.getReason() + "\n[Seller Note]: " + request.getNote().trim();
                refund.setReason(updatedReason);
            }

            orderRefundRepository.save(refund);

            log.info("Refund approved by seller. RefundId: {}, SellerId: {}", refundId, request.getSellerId());

            return ApiResponse.<OrderRefundDto>builder()
                    .data(transactionMapper.toOrderRefundDto(refund))
                    .message("Refund approved successfully")
                    .build();

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error approving refund. RefundId: {}, SellerId: {}", refundId, request.getSellerId(), e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public ApiResponse<OrderRefundDto> rejectRefund(UUID refundId, RefundRejectRequest request) {
        // Validate input
        if (refundId == null || request.getSellerId() == null || request.getReason() == null || request.getReason().trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_PARAMETER);
        }

        try {
            // Find refund and validate it belongs to seller
            OrderRefund refund = orderRefundRepository.findByIdAndSellerId(refundId, request.getSellerId())
                    .orElseThrow(() -> new CustomException(ErrorCode.REFUND_FOR_ORDER_NOT_FOUND));

            // Validate current status
            if (!Status.PENDING.equals(refund.getStatus())) {
                throw new CustomException(ErrorCode.INVALID_PARAMETER);
            }

            // Update status to REJECTED
            refund.setStatus(Status.REJECTED);
            
            // Append rejection reason
            String updatedReason = refund.getReason() + "\n[Seller Rejection]: " + request.getReason().trim();
            refund.setReason(updatedReason);

            orderRefundRepository.save(refund);

            log.info("Refund rejected by seller. RefundId: {}, SellerId: {}", refundId, request.getSellerId());

            return ApiResponse.<OrderRefundDto>builder()
                    .data(transactionMapper.toOrderRefundDto(refund))
                    .message("Refund rejected successfully")
                    .build();

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error rejecting refund. RefundId: {}, SellerId: {}", refundId, request.getSellerId(), e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public ApiResponse<OrderRefundDto> updateRefundStatus(UUID refundId, RefundStatusUpdateRequest request) {
        // Validate input
        if (refundId == null || request.getStatus() == null || request.getUpdatedBy() == null) {
            throw new CustomException(ErrorCode.INVALID_PARAMETER);
        }

        try {
            // Find refund
            OrderRefund refund = orderRefundRepository.findById(refundId)
                    .orElseThrow(() -> new CustomException(ErrorCode.REFUND_FOR_ORDER_NOT_FOUND));

            // Validate status transition
            if (!isValidStatusTransition(refund.getStatus(), request.getStatus())) {
                throw new CustomException(ErrorCode.INVALID_PARAMETER);
            }

            // Update status
            refund.setStatus(request.getStatus());
            
            // If status is completed, set completed_at
            if (Status.COMPLETED.equals(request.getStatus())) {
                refund.setCompletedAt(LocalDateTime.now());
            }

            // Append admin note if provided
            if (request.getNote() != null && !request.getNote().trim().isEmpty()) {
                String updatedReason = refund.getReason() + "\n[Admin Note]: " + request.getNote().trim();
                refund.setReason(updatedReason);
            }

            orderRefundRepository.save(refund);

            log.info("Refund status updated. RefundId: {}, NewStatus: {}, UpdatedBy: {}", 
                    refundId, request.getStatus(), request.getUpdatedBy());

            return ApiResponse.<OrderRefundDto>builder()
                    .data(transactionMapper.toOrderRefundDto(refund))
                    .message("Refund status updated successfully")
                    .build();

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating refund status. RefundId: {}, Status: {}", refundId, request.getStatus(), e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional
    public ApiResponse<OrderRefundDto> processRefundPayment(UUID refundId, RefundProcessPaymentRequest request) {
        // Validate input
        if (refundId == null || request.getAdminId() == null || request.getPaymentMethod() == null) {
            throw new CustomException(ErrorCode.INVALID_PARAMETER);
        }

        try {
            // Find refund
            OrderRefund refund = orderRefundRepository.findById(refundId)
                    .orElseThrow(() -> new CustomException(ErrorCode.REFUND_FOR_ORDER_NOT_FOUND));

            // Validate current status must be APPROVED
            if (!Status.APPROVED.equals(refund.getStatus())) {
                throw new CustomException(ErrorCode.INVALID_PARAMETER);
            }

            // Update status to PROCESSING
            refund.setStatus(Status.PROCESSING);
            
            // Add payment processing note
            String paymentNote = String.format("[Payment Processing]: Method=%s, AdminId=%s", 
                    request.getPaymentMethod(), request.getAdminId());
            if (request.getNote() != null && !request.getNote().trim().isEmpty()) {
                paymentNote += ", Note=" + request.getNote().trim();
            }
            
            String updatedReason = refund.getReason() + "\n" + paymentNote;
            refund.setReason(updatedReason);

            orderRefundRepository.save(refund);

            // TODO: Integrate with payment gateway here
            // For now, we'll simulate success and update to COMPLETED
            // In real implementation, this would be async and status would be updated via callback
            
            log.info("Refund payment processing started. RefundId: {}, Method: {}, AdminId: {}", 
                    refundId, request.getPaymentMethod(), request.getAdminId());

            return ApiResponse.<OrderRefundDto>builder()
                    .data(transactionMapper.toOrderRefundDto(refund))
                    .message("Refund payment processing started")
                    .build();

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error processing refund payment. RefundId: {}, AdminId: {}", refundId, request.getAdminId(), e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Validate status transitions according to business rules
     */
    private boolean isValidStatusTransition(Status currentStatus, Status newStatus) {
        return switch (currentStatus) {
            case PENDING -> newStatus == Status.APPROVED || newStatus == Status.REJECTED;
            case APPROVED -> newStatus == Status.PROCESSING || newStatus == Status.CANCELLED;
            case PROCESSING -> newStatus == Status.COMPLETED || newStatus == Status.CANCELLED;
            case REJECTED, COMPLETED -> false; // No further changes allowed
            default -> false;
        };
    }
}