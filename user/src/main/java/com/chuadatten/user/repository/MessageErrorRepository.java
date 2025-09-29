package com.chuadatten.user.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.chuadatten.user.entity.SendMessageError;

@Repository
public interface MessageErrorRepository extends JpaRepository<SendMessageError, UUID> {
    
    Page<SendMessageError> findByStatus(String status, Pageable pageable);
    
    // Since resolved field doesn't exist, we'll use status to simulate resolved state
    @Query("SELECT sme FROM SendMessageError sme WHERE sme.status = 'RESOLVED'")
    Page<SendMessageError> findByResolved(Boolean resolved, Pageable pageable);
    
    @Query("SELECT sme FROM SendMessageError sme WHERE sme.status = :status")
    Page<SendMessageError> findByErrorTypeAndResolved(String status, Boolean resolved, Pageable pageable);
}
