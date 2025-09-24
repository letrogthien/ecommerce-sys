package com.chuadatten.wallet.service.impl;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.chuadatten.event.PayUrlEvent;
import com.chuadatten.event.PaymentProcessEvent;
import com.chuadatten.event.PaymentSuccessedEvent;
import com.chuadatten.wallet.common.JsonParserUtil;
import com.chuadatten.wallet.common.PaymentMethod;
import com.chuadatten.wallet.common.PaymentType;
import com.chuadatten.wallet.common.Status;
import com.chuadatten.wallet.dto.PaymentAttemptDto;
import com.chuadatten.wallet.dto.PaymentDto;
import com.chuadatten.wallet.entity.Payment;
import com.chuadatten.wallet.entity.PaymentAttempt;
import com.chuadatten.wallet.entity.Wallet;
import com.chuadatten.wallet.entity.WalletReservation;
import com.chuadatten.wallet.exceptions.CustomException;
import com.chuadatten.wallet.exceptions.ErrorCode;
import com.chuadatten.wallet.kafka.KafkaTopic;
import com.chuadatten.wallet.mapper.PaymentAttemptMapper;
import com.chuadatten.wallet.mapper.PaymentMapper;
import com.chuadatten.wallet.outbox.OutboxEvent;
import com.chuadatten.wallet.outbox.OutboxRepository;
import com.chuadatten.wallet.repository.PaymentAttemptRepository;
import com.chuadatten.wallet.repository.PaymentRepository;
import com.chuadatten.wallet.repository.WalletRepository;
import com.chuadatten.wallet.repository.WalletReservationRepository;
import com.chuadatten.wallet.responses.ApiResponse;
import com.chuadatten.wallet.service.PaymentService;
import com.chuadatten.wallet.vnpay.IPNReturn;
import com.chuadatten.wallet.vnpay.VnpayReturnDto;
import com.chuadatten.wallet.vnpay.VnpayUltils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final PaymentAttemptRepository paymentAttemptRepository;
    private final VnpayUltils vnpayUltils;
    private final PaymentAttemptMapper paymentAttemptMapper;
    private final OutboxRepository outboxRepository;
    private final JsonParserUtil jsonParserUtil;
    private final WalletReservationRepository walletReservationRepository;
    private final WalletRepository walletRepository;

    @Override
    public ApiResponse<String> payment(UUID paymentId, String ip, UUID userId, PaymentMethod paymentMethod)
            throws InvalidKeyException, NoSuchAlgorithmException, JsonProcessingException {

        Payment pay = paymentRepository.findById(paymentId).orElseThrow(
                () -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));
        if (!userId.equals(pay.getUserId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        if (!pay.getStatus().equals(Status.CREATED)) {
            handlePaymentStatus(pay.getStatus());
        }
        if (paymentMethod.equals(PaymentMethod.WALLET)) {
            Wallet wallet = walletRepository.findById(userId).orElseThrow(
                    () -> new CustomException(ErrorCode.WALLET_NOT_FOUND));
            if (wallet.getBalance().compareTo(pay.getAmount()) < 0) {
                throw new CustomException(ErrorCode.INSUFFICIENT_BALANCE);
            }
            wallet.setBalance(wallet.getBalance().subtract(pay.getAmount()));
            walletRepository.save(wallet);
            WalletReservation walletReservation = WalletReservation.builder()
                    .amount(pay.getAmount())
                    .currency(pay.getCurrency())
                    .orderId(UUID.fromString(pay.getOrderId().toString()))
                    .walletId(wallet.getId())
                    .status(Status.ACTIVE)
                    .build();
            walletReservationRepository.save(walletReservation);

        }

        OutboxEvent outboxEvent = OutboxEvent.builder()
                .aggregateId(paymentId.toString())
                .aggregateType("PAYMENT")
                .attempts(0)
                .eventType(KafkaTopic.PAYMENT_PROCECSSING.name())
                .payload(jsonParserUtil.toJson(PaymentProcessEvent.builder().orderId(pay.getOrderId().toString())
                        .paymentId(pay.getId().toString()).ip(ip).paymentMethod(paymentMethod.toString()).build()))
                .build();
        outboxRepository.save(outboxEvent);
        pay.setStatus(Status.PROCESSING);

        return ApiResponse.<String>builder()
                .data("payment processing, please wait")
                .build();

    }

    private void handlePaymentStatus(Status status) {
        switch (status) {
            case CREATED:
                throw new CustomException(ErrorCode.PAYMENT_CREATED);
            case PROCESSING:
                throw new CustomException(ErrorCode.PAYMENT_PROCESSING);
            case SUCCESS:
                throw new CustomException(ErrorCode.PAYMENT_SUCCESS);

            case FAILED:
                throw new CustomException(ErrorCode.PAYMENT_FAILED);
            case CANCELED:
                throw new CustomException(ErrorCode.PAYMENT_CANCELED);
            case REFUNDED:
                throw new CustomException(ErrorCode.PAYMENT_REFUNDED);

            default:
                throw new CustomException(ErrorCode.PAYMENT_ERROR);
        }
    }

    @Override
    public ApiResponse<String> retryPayment(UUID paymentId, String ip)
            throws InvalidKeyException, NoSuchAlgorithmException, JsonProcessingException {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

        if (!payment.getStatus().equals(Status.PROCESSING)) {
            throw new CustomException(ErrorCode.PAYMENT_NOT_IN_PROCESSING);
        }
        List<PaymentAttempt> attempts = paymentAttemptRepository.findAllByPaymentIdAndExpiredAtAfter(paymentId,
                java.time.LocalDateTime.now());
        if (!attempts.isEmpty()) {
            PaymentAttempt lastAttempt = attempts.getFirst();
            PayUrlEvent payUrlEvent = PayUrlEvent.builder()
                    .orderId(payment.getOrderId().toString())
                    .paymentId(payment.getId().toString())
                    .payUrl(lastAttempt.getAttemptData())
                    .userId(payment.getUserId().toString())
                    .build();

            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .eventType(KafkaTopic.PAYMENT_URL_SUCCESS.name())
                    .payload(jsonParserUtil.toJson(payUrlEvent))
                    .aggregateId(payment.getId().toString())
                    .aggregateType("PAYMENT")
                    .status("PENDING")
                    .build();
            outboxRepository.save(outboxEvent);

            return ApiResponse.<String>builder()
                    .data("Payment is processing, please wait")
                    .build();

        }
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .aggregateId(paymentId.toString())
                .aggregateType("PAYMENT")
                .attempts(0)
                .eventType(KafkaTopic.PAYMENT_PROCECSSING.name())
                .payload(jsonParserUtil.toJson(PaymentProcessEvent.builder().orderId(payment.getOrderId().toString())
                        .paymentId(payment.getId().toString()).ip(ip).paymentMethod(PaymentMethod.DIRECT.name())
                        .build()))
                .build();
        outboxRepository.save(outboxEvent);
        return ApiResponse.<String>builder()
                .data("Payment is processing, please wait")
                .build();

    }

    @Override
    public ApiResponse<PaymentDto> getPaymentStatus(UUID paymentId) {
        return getPaymentById(paymentId);
    }

    @Override
    public ApiResponse<PaymentDto> getPaymentById(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));
        PaymentDto paymentDto = paymentMapper.toDto(payment);
        return ApiResponse.<PaymentDto>builder()
                .data(paymentDto)
                .build();
    }

    @Override
    public ApiResponse<PaymentDto> getPaymentByOrderId(UUID orderId) {
        List<Payment> payments = paymentRepository.findByOrderId(orderId);
        if (payments.isEmpty()) {
            throw new CustomException(ErrorCode.PAYMENT_NOT_FOUND);
        }
        Payment payment = payments.get(0);
        PaymentDto paymentDto = paymentMapper.toDto(payment);
        return ApiResponse.<PaymentDto>builder()
                .data(paymentDto)
                .build();
    }

    @Override
    public ApiResponse<Page<PaymentDto>> getUserPayments(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PaymentDto> payments = paymentRepository.findAllByUserId(userId, pageable)
                .map(paymentMapper::toDto);
        return ApiResponse.<Page<PaymentDto>>builder()
                .data(payments)
                .build();
    }

    @Override
    @Transactional
    public IPNReturn handleProviderCallback(VnpayReturnDto returnDto)
            throws InvalidKeyException, NoSuchAlgorithmException, JsonProcessingException {

        if (returnDto.getResponseCode() == null || !"00".equals(returnDto.getResponseCode())) {
            throw new CustomException(ErrorCode.PAYMENT_FAILED);

        }
        String id = returnDto.getTxnRef().substring(0, returnDto.getTxnRef().length() - 13);

        Payment payment = paymentRepository.findById(UUID.fromString(
                id))
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));
        payment.setStatus(Status.SUCCEEDED);

        List<PaymentAttempt> attempts = paymentAttemptRepository.findAllByPaymentId(payment.getId());
        for (PaymentAttempt attempt : attempts) {
            if (attempt.getStatus() == Status.SUCCEEDED) {
                return IPNReturn.builder()
                        .rspCode("00")
                        .message("Success")
                        .build();
            }
        }

        PaymentAttempt paymentAttempt = new PaymentAttempt();
        ObjectMapper mapper = new ObjectMapper();
        paymentAttempt.setProviderResponse(mapper.writeValueAsString(returnDto));
        paymentAttempt.setPaymentId(payment.getId());
        paymentAttempt.setStatus(Status.SUCCEEDED);

        PaymentSuccessedEvent paymentSuccessedEvent = PaymentSuccessedEvent.builder()
                .orderId(payment.getOrderId().toString())
                .paymentId(payment.getId().toString())
                .userId(payment.getUserId().toString())
                .build();
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .eventType(KafkaTopic.PAYMENT_SUCCESS.name())
                .payload(jsonParserUtil.toJson(paymentSuccessedEvent))
                .aggregateId(payment.getId().toString())
                .aggregateType("PAYMENT")
                .status("PENDING")
                .build();

        outboxRepository.save(outboxEvent);
        paymentRepository.save(payment);
        paymentAttemptRepository.save(paymentAttempt);
        return IPNReturn.builder()
                .rspCode("00")
                .message("Success")
                .build();

    }

    @Override
    public void cancelPayment(UUID paymentId) {
        throw new UnsupportedOperationException("Unimplemented method 'cancelPayment'");
    }

    @Override
    public void refundPayment(UUID paymentId, UUID sellerId, String ip)
            throws JsonProcessingException, InvalidKeyException, NumberFormatException, NoSuchAlgorithmException {
        // TODO: Implement refund payment logic
    }

    @Override
    public ApiResponse<PaymentAttemptDto> getPaymentAttempts(UUID paymentId) {
        PaymentAttempt paymentAttempt = paymentAttemptRepository.findByPaymentId(paymentId);
        return ApiResponse.<PaymentAttemptDto>builder()
                .data(paymentAttemptMapper.toDto(paymentAttempt))
                .build();
    }

    @Override
    public ApiResponse<Void> depositWallet(UUID userId, double amount, String ip) throws InvalidKeyException, NoSuchAlgorithmException {
        Payment payment = Payment.builder()
                .userId(userId)
                .paymentMethod(PaymentMethod.DIRECT)
                .amount(java.math.BigInteger.valueOf((long) (amount * 100))) // Convert to cents
                .currency("USD")
                .type(PaymentType.WALLET_DEPOSIT)
                .status(Status.CREATED)
                .build();
        paymentRepository.save(payment);
        String payUrl =vnpayUltils.payUrl(ip,payment.getAmount(),payment.getId().toString(), payment.getId().toString());
        PaymentAttempt paymentAttempt = PaymentAttempt.builder()
                .paymentId(payment.getId())
                .status(Status.CREATED)
                .attemptData(payUrl)
                .build();

        paymentAttemptRepository.save(paymentAttempt);
        return ApiResponse.<Void>builder()
                .message("Wallet deposit initiated")
                .build();

        
    }

}
