package com.chuadatten.wallet.repository;

import com.chuadatten.wallet.entity.WalletReservation;

import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletReservationRepository extends BaseRepository<WalletReservation> {

    List<WalletReservation> findByWalletIdAndStatus(UUID walletId, String status);

    List<WalletReservation> findByWalletId(UUID walletId);

    Optional<WalletReservation> findByOrderId(UUID orderId);

    List<WalletReservation> findByExpiresAtBefore(LocalDateTime dateTime);


}
