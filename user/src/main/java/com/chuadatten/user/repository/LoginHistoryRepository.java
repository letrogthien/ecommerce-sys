package com.chuadatten.user.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.chuadatten.user.entity.LoginHistory;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, UUID> {
    
    @Query("SELECT lh FROM LoginHistory lh WHERE lh.user.id = :userId")
    Page<LoginHistory> findByUserId(@Param("userId") UUID userId, Pageable pageable);
    
    Page<LoginHistory> findBySuccess(Boolean success, Pageable pageable);
    
    @Query("SELECT lh FROM LoginHistory lh WHERE lh.user.id = :userId AND lh.success = :success")
    Page<LoginHistory> findByUserIdAndSuccess(@Param("userId") UUID userId, @Param("success") Boolean success, Pageable pageable);
    
    Page<LoginHistory> findByLoginAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    @Query("SELECT COUNT(lh) FROM LoginHistory lh WHERE lh.loginAt >= :startDate")
    Long countByLoginAtAfter(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT COUNT(lh) FROM LoginHistory lh WHERE lh.loginAt BETWEEN :startDate AND :endDate")
    Long countByLoginAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}