package com.chuadatten.wallet.service;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import org.springframework.data.domain.Page;

import com.chuadatten.wallet.common.PaymentMethod;
import com.chuadatten.wallet.dto.PaymentAttemptDto;
import com.chuadatten.wallet.dto.PaymentDto;
import com.chuadatten.wallet.responses.ApiResponse;
import com.chuadatten.wallet.vnpay.VnpayReturnDto;
import com.fasterxml.jackson.core.JsonProcessingException;


public interface PaymentService {


    /**
     * Query payment status
     * 
     * @param paymentId ID of the payment
     * @return ApiResponse containing payment information
     */
    ApiResponse<PaymentDto> getPaymentStatus(UUID paymentId);


    /**
     * Get payment by ID
     * 
     * @param paymentId ID of the payment
     * @return ApiResponse containing payment information
     */
    ApiResponse<PaymentDto> getPaymentById(UUID paymentId);


    /**
     * Get payment by order ID
     * 
     * @param orderId ID of the order
     * @return ApiResponse containing payment information
     */
    ApiResponse<PaymentDto> getPaymentByOrderId(UUID orderId);





    /**
     * Get all payments of a user
     * 
     * @param userId ID of the user
     * @param page   Page number
     * @param size   Page size
     * @return ApiResponse containing paginated list of payments
     */
    ApiResponse<Page<PaymentDto>> getUserPayments(UUID userId, int page, int size);

    /**
     * Handle callback from provider (VNPAY, etc.)
     * 
     * @param request CreatePaymentAttemptRequest containing provider name and
     *                callback data
     * @return ApiResponse containing updated payment information
     */
    ApiResponse<PaymentDto> handleProviderCallback(VnpayReturnDto request) throws InvalidKeyException, NoSuchAlgorithmException,JsonProcessingException;

    /**
     * Payment
     * @param paymentId
     * @return url payment
     * @throws NoSuchAlgorithmException 
     * @throws InvalidKeyException 
     */

     ApiResponse<String> payment(UUID paymentId, String ip , UUID userId, PaymentMethod paymentMethod) throws InvalidKeyException, NoSuchAlgorithmException,JsonProcessingException;
    /**
     * Retry payment when in pending status
     * 
     */
    ApiResponse<String> retryPayment(UUID paymentId);

    /**
     * Cancel payment
     */
    void cancelPayment(UUID paymentId);

    /**
     * Refund payment
     * 
     */
    void refundPayment(UUID paymentId,UUID sellerId, String ip) throws  JsonProcessingException, InvalidKeyException, NumberFormatException, NoSuchAlgorithmException;

    /**
     * Get payment attempt history
     * 
     * @param paymentId ID of the payment
     * @return ApiResponse containing list of payment attempts
     */
    ApiResponse<PaymentAttemptDto> getPaymentAttempts(UUID paymentId);
}
