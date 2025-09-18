package com.chuadatten.user.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.chuadatten.user.entity.UserVerification;


@Repository
public interface UserVerificationRepository extends JpaRepository<UserVerification, UUID> {
    Optional<UserVerification> findTopByUserIdOrderByVersionDesc(UUID userId);

    Optional<UserVerification> findByUserId(UUID id);
}
