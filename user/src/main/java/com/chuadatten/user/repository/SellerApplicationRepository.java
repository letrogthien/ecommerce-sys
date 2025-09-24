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

import com.chuadatten.user.entity.SellerApplication;

@Repository
public interface SellerApplicationRepository extends JpaRepository<SellerApplication, UUID>{
    Optional<SellerApplication> findByUserId(UUID userId);
    
    Page<SellerApplication> findByApplicationStatus(String status, Pageable pageable);
    
    @Query("SELECT COUNT(sa) FROM SellerApplication sa WHERE sa.createdAt BETWEEN :startDate AND :endDate")
    Long countByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
