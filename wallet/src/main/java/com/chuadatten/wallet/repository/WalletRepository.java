package com.chuadatten.wallet.repository;

import com.chuadatten.wallet.common.Status;
import com.chuadatten.wallet.entity.Wallet;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletRepository extends BaseRepository<Wallet> {

    Optional<Wallet> findByUserIdAndCurrency(UUID userId, String currency);

    List<Wallet> findByUserId(UUID userId);

    List<Wallet> findByStatus(Status status);

    List<Wallet> findByUserIdAndStatus(UUID userId, Status status);

}
