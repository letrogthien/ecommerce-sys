package com.chuadatten.wallet.repository;

import com.chuadatten.wallet.entity.PaymentAttempt;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentAttemptRepository extends BaseRepository<PaymentAttempt> {

    PaymentAttempt findByPaymentId(UUID paymentId);

    List<PaymentAttempt> findByPaymentIdOrderByCreatedAtDesc(UUID paymentId);

    List<PaymentAttempt> findAllByPaymentIdAndExpiredAtAfter(UUID paymentId, java.time.LocalDateTime now);

    List<PaymentAttempt> findAllByPaymentId(UUID id);
}
