package com.chuadatten.user.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.chuadatten.user.common.Status;
import com.chuadatten.user.entity.UserInf;

@Repository
public interface UserInfRepository extends JpaRepository<UserInf, UUID> {
    Optional<UserInf> findByEmail(String email);
    boolean existsByEmail(String email);
    Page<UserInf> findAllByStatus(Status status, Pageable pageable);
    Optional<UserInf> findByDisplayName(String displayName);
    
    Long countByStatus(Status status);
}
