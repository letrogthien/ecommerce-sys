package com.chuadatten.wallet.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import com.chuadatten.wallet.common.Status;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payment_attempts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "payment_id", nullable = false)
    private UUID paymentId;

    @Column(name = "attempt_data")
    private String attemptData;

    @Column(name = "provider_response", columnDefinition = "JSON")
    private String providerResponse;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30)
    private Status status;

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (status == null) {
            status = Status.PENDING;
        }
        if (expiredAt == null) {
            expiredAt = LocalDateTime.now().plusMinutes(15);
        }
    }
}
