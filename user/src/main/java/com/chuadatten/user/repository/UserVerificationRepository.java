package com.chuadatten.user.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.chuadatten.user.common.Status;
import com.chuadatten.user.entity.UserVerification;


@Repository
public interface UserVerificationRepository extends JpaRepository<UserVerification, UUID> {
    Optional<UserVerification> findTopByUserIdOrderByVersionDesc(UUID userId);

    Optional<UserVerification> findByUserId(UUID id);
    
    Page<UserVerification> findByVerificationStatus(Status status, Pageable pageable);
    
    @Query("SELECT COUNT(uv) FROM UserVerification uv WHERE uv.createdAt BETWEEN :startDate AND :endDate")
    Long countByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
