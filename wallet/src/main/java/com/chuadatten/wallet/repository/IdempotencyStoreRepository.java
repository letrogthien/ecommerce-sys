package com.chuadatten.wallet.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.chuadatten.wallet.entity.IdempotencyStore;

@Repository
public interface IdempotencyStoreRepository extends BaseRepository<IdempotencyStore> {
    boolean existsByIdempotencyKey(String idempotencyKey);
    Optional<IdempotencyStore> findByIdempotencyKey(String idempotencyKey);
}
