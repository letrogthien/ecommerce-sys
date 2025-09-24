package com.chuadatten.user.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.chuadatten.user.entity.AuditLog;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    Page<AuditLog> findByUserId(UUID userId, Pageable pageable);
    Page<AuditLog> findByActionContaining(String action, Pageable pageable);
    Page<AuditLog> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    @Query("SELECT al FROM AuditLog al WHERE al.userId = :userId AND al.action LIKE CONCAT('%', :action, '%')")
    Page<AuditLog> findByUserIdAndActionContaining(@Param("userId") UUID userId, @Param("action") String action, Pageable pageable);

    @Query("SELECT al FROM AuditLog al WHERE " +
           "(:action IS NULL OR al.action LIKE CONCAT('%', :action, '%')) AND " +
           "(:startDate IS NULL OR al.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR al.createdAt <= :endDate)")
    Page<AuditLog> findByFilters(
        @Param("action") String action,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable);
}
