package com.chuadatten.transaction.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.chuadatten.transaction.common.Status;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @Column(columnDefinition = "BINARY(16)")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "buyer_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID buyerId;

    @Column(name = "seller_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID sellerId;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Column(length = 3, nullable = false)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private Status paymentStatus;

    @Column(name = "audit_flag", nullable = false)
    private boolean auditFlag;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    // Relations
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderProof> proofs;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderRefund> refunds;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDispute> disputes;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderLog> logs;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        auditFlag = false;
        status = Status.CREATED;
        paymentStatus = Status.PENDING;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }



}