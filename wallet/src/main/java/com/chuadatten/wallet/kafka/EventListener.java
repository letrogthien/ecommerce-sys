package com.chuadatten.wallet.kafka;

import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.chuadatten.event.OrderCancel;
import com.chuadatten.event.OrderCheckingStatusEvent;
import com.chuadatten.event.OrderComfirmData;
import com.chuadatten.event.PayUrlEvent;
import com.chuadatten.event.PaymentSuccessedEvent;
import com.chuadatten.wallet.common.JsonParserUtil;
import com.chuadatten.wallet.common.PaymentMethod;
import com.chuadatten.wallet.common.PaymentType;
import com.chuadatten.wallet.common.Status;
import com.chuadatten.wallet.entity.IdempotencyStore;
import com.chuadatten.wallet.entity.Payment;
import com.chuadatten.wallet.entity.PaymentAttempt;
import com.chuadatten.wallet.entity.Wallet;
import com.chuadatten.wallet.entity.WalletReservation;
import com.chuadatten.wallet.entity.WalletTransaction;
import com.chuadatten.wallet.exceptions.CustomException;
import com.chuadatten.wallet.exceptions.ErrorCode;
import com.chuadatten.wallet.outbox.OutboxEvent;
import com.chuadatten.wallet.outbox.OutboxRepository;
import com.chuadatten.wallet.repository.IdempotencyStoreRepository;
import com.chuadatten.wallet.repository.PaymentAttemptRepository;
import com.chuadatten.wallet.repository.PaymentRepository;
import com.chuadatten.wallet.repository.WalletRepository;
import com.chuadatten.wallet.repository.WalletReservationRepository;
import com.chuadatten.wallet.repository.WalletTransactionRepository;
import com.chuadatten.wallet.vnpay.VnpayUltils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventListener {
        private final PaymentRepository paymentRepository;
        private final IdempotencyStoreRepository idempotencyStoreRepository;
        private final VnpayUltils vnpayUltils;
        private final JsonParserUtil jsonParserUtil;
        private final OutboxRepository outboxRepository;
        private final PaymentAttemptRepository paymentAttemptRepository;
        private final WalletRepository walletRepository;
        private final WalletReservationRepository walletReservationRepository;
        private final WalletTransactionRepository walletTransactionRepository;

        @Transactional
        @KafkaListener(topics = "transaction.order.confirm.data", groupId = "wallet-group")
        public void listenOrderConfirmData(OrderComfirmData event) {
                handleOrderConfirmDataListener(event);
        }

        @KafkaListener(topics = "transaction.order.cancel", groupId = "wallet-group")
        public void listenOrderCancel(OrderCancel event) {
                handleOrderCancelListener(event);
        }

        private void handleOrderCancelListener(OrderCancel event) {
                Payment pay = paymentRepository.findByOrderId(UUID.fromString(event.getOrderId())).getFirst();

                if (pay.getStatus().equals(Status.SUCCESS) || pay.getStatus().equals(Status.CANCELED)) {
                        return;
                }
                pay.setStatus(Status.CANCELED);
                paymentRepository.save(pay);
                if (pay.getPaymentMethod().equals(PaymentMethod.WALLET)) {
                        WalletReservation walletReservation = walletReservationRepository
                                        .findByOrderId(UUID.fromString(event.getOrderId()))
                                        .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_SUCCEEDED));
                        walletReservation.setStatus(Status.CANCELED);
                        Wallet wallet = walletRepository.findById(walletReservation.getWalletId())
                                        .orElseThrow(() -> new CustomException(ErrorCode.WALLET_NOT_FOUND));
                        wallet.setReserved(wallet.getReserved().subtract(walletReservation.getAmount()));
                        wallet.setBalance(wallet.getBalance().add(walletReservation.getAmount()));
                        walletRepository.save(wallet);
                        walletReservationRepository.save(walletReservation);

                        WalletTransaction walletTransaction = WalletTransaction.builder()
                                        .amount(walletReservation.getAmount())
                                        .currency(walletReservation.getCurrency())
                                        .type(PaymentType.ORDER_PAYMENT.toString())
                                        .walletId(wallet.getId())
                                        .status(Status.CANCELED)
                                        .build();
                        walletTransactionRepository.save(walletTransaction);
                }

        }

        private void handleOrderConfirmDataListener(OrderComfirmData event) {
                if (idempotencyStoreRepository.existsByIdempotencyKey(event.getIdempotencyKey())) {
                        return;
                }
                BigDecimal amount = event.getTotalAmount();
                Payment payment = Payment.builder()
                                .orderId(UUID.fromString(event.getOrderId()))
                                .amount(amount.toBigInteger())
                                .status(Status.CREATED)
                                .currency("VND")
                                .idempotencyKey(event.getIdempotencyKey())
                                .userId(UUID.fromString(event.getBuyerId()))
                                .type(PaymentType.ORDER_PAYMENT)
                                .build();
                paymentRepository.save(payment);

                IdempotencyStore idempotencyStore = IdempotencyStore.builder()
                                .idempotencyKey(event.getIdempotencyKey())
                                .resourceId(payment.getId())
                                .resourceType("PAYMENT")
                                .build();
                idempotencyStoreRepository.save(idempotencyStore);
        }

        @KafkaListener(topics = "order.checking.status", groupId = "wallet-group")
        public void listenOrderCheckingStatus(OrderCheckingStatusEvent event)
                        throws InvalidKeyException, NoSuchAlgorithmException, JsonProcessingException {
                handleOrderCheckingStatusListener(event);
        }

        private void handleOrderCheckingStatusListener(OrderCheckingStatusEvent event)
                        throws InvalidKeyException, NoSuchAlgorithmException, JsonProcessingException {
                Payment pay = paymentRepository.findById(UUID.fromString(event.getPaymentId())).orElse(null);

                if (pay == null) {
                        throw new CustomException(ErrorCode.PAYMENT_NOT_FOUND);
                }
                if (pay.getStatus().equals(Status.SUCCESS) || pay.getStatus().equals(Status.CANCELED)) {
                        return;
                }
                pay.setMetadata(event.getOrderId());

                paymentRepository.save(pay);
                if (event.getStatus().equals("OK")) {

                        if (event.getPaymentMethod().equals(PaymentMethod.DIRECT.name())) {
                                String url = vnpayUltils.payUrl(event.getIp(), pay.getAmount(), pay.getMetadata(),
                                                pay.getId().toString());
                                pay.setStatus(Status.PROCESSING);

                                paymentRepository.save(pay);
                                PayUrlEvent payUrlEvent = PayUrlEvent.builder()
                                                .orderId(event.getOrderId())
                                                .paymentId(pay.getId().toString())
                                                .payUrl(url)
                                                .userId(pay.getUserId().toString())
                                                .build();

                                OutboxEvent outboxEvent = OutboxEvent.builder()
                                                .eventType(KafkaTopic.PAYMENT_URL_SUCCESS.name())
                                                .payload(jsonParserUtil.toJson(payUrlEvent))
                                                .aggregateId(pay.getId().toString())
                                                .aggregateType("PAYMENT")
                                                .status("PENDING")
                                                .build();
                                outboxRepository.save(outboxEvent);

                                PaymentAttempt paymentAttempt = new PaymentAttempt();
                                paymentAttempt.setAttemptData(url);
                                paymentAttempt.setPaymentId(UUID.fromString(event.getPaymentId()));
                                paymentAttempt.setStatus(Status.CREATED);

                                paymentAttemptRepository.save(paymentAttempt);
                                return;
                        }

                        WalletReservation walletReservation = walletReservationRepository
                                        .findByOrderId(UUID.fromString(event.getOrderId()))
                                        .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_SUCCEEDED));
                        walletReservation.setStatus(Status.COMPLETED);
                        Wallet wallet = walletRepository.findById(walletReservation.getWalletId())
                                        .orElseThrow(() -> new CustomException(ErrorCode.WALLET_NOT_FOUND));
                        if (wallet.getReserved().compareTo(walletReservation.getAmount()) < 0) {
                                throw new CustomException(ErrorCode.WALLET_INSUFFICIENT_RESERVED);
                        }
                        wallet.setReserved(wallet.getReserved().subtract(walletReservation.getAmount()));

                        walletRepository.save(wallet);
                        walletReservationRepository.save(walletReservation);
                        pay.setStatus(Status.SUCCESS);

                        WalletTransaction walletTransaction = WalletTransaction.builder()
                                        .amount(walletReservation.getAmount())
                                        .currency(walletReservation.getCurrency())
                                        .type(PaymentType.ORDER_PAYMENT.toString())
                                        .walletId(wallet.getId())
                                        .status(Status.SUCCESS)
                                        .build();
                        walletTransactionRepository.save(walletTransaction);

                        PaymentSuccessedEvent paymentSuccessedEvent = PaymentSuccessedEvent.builder()
                                        .orderId(pay.getOrderId().toString())
                                        .paymentId(pay.getId().toString())
                                        .userId(pay.getUserId().toString())
                                        .build();
                        OutboxEvent outboxEvent = OutboxEvent.builder()
                                        .eventType(KafkaTopic.PAYMENT_SUCCESS.name())
                                        .payload(jsonParserUtil.toJson(paymentSuccessedEvent))
                                        .aggregateId(pay.getId().toString())
                                        .aggregateType("PAYMENT")
                                        .status("PENDING")
                                        .build();

                        outboxRepository.save(outboxEvent);

                        PaymentAttempt paymentAttempt = new PaymentAttempt();
                        paymentAttempt.setAttemptData("Wallet payment success");
                        paymentAttempt.setPaymentId(UUID.fromString(event.getPaymentId()));
                        paymentAttempt.setStatus(Status.CREATED);

                        paymentAttemptRepository.save(paymentAttempt);
                }
        }

}
