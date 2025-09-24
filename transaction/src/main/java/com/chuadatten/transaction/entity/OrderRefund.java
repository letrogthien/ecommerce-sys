package com.chuadatten.transaction.entity;
import java.time.LocalDateTime;
import java.util.UUID;

import com.chuadatten.transaction.common.Status;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



@Entity
@Table(name = "order_refunds")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRefund {

    @Id
    @Column(columnDefinition = "BINARY(16)")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "request_by", columnDefinition = "BINARY(16)", nullable = false)
    private UUID requestBy;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private Status status;

    @Column(columnDefinition = "TEXT")
    private String reason;
    
    // Virtual fields for admin notes and updates (managed in application layer)
    // These will be stored as part of reason or separate logging
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // Getters & Setters
    // ...
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        status = Status.PENDING;
    }
}
