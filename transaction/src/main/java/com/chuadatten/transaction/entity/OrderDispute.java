package com.chuadatten.transaction.entity;
import java.time.LocalDateTime;
import java.util.UUID;
import com.chuadatten.transaction.common.DisputeIssueType;
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
@Table(name = "order_disputes")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDispute {

    @Id
    @Column(columnDefinition = "BINARY(16)")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "opened_by", columnDefinition = "BINARY(16)", nullable = false)
    private UUID openedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "issue_type", length = 50, nullable = false)
    private DisputeIssueType issueType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private Status status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;


    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        status = Status.OPENED;
        issueType = DisputeIssueType.OTHER;
    }

}
