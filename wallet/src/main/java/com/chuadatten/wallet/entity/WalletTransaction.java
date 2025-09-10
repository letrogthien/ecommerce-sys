package com.chuadatten.wallet.entity;

import com.chuadatten.wallet.common.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "wallet_transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "wallet_id", nullable = false)
    private UUID walletId;

    @Column(name = "related_entity_id")
    private UUID relatedEntityId;

    @Column(name = "type", nullable = false, length = 40)
    private String type;

    @Column(name = "amount", nullable = false)
    private BigInteger amount;

    @Column(name = "direction", nullable = false)
    private Integer direction;

    @Column(name = "balance_after", nullable = false)
    private BigInteger balanceAfter;

    @Column(name = "currency", nullable = false, columnDefinition = "CHAR(3)")
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private Status status;

    @Column(name = "idempotency_key", length = 255)
    private String idempotencyKey;

    @Column(name = "metadata", columnDefinition = "JSON")
    private String metadata;

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
    }
}
