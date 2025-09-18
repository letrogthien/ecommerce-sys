package com.chuadatten.product.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.chuadatten.product.entity.ProductVariant;

@Repository
public interface ProductVariantRepository extends MongoRepository<ProductVariant, String> {
    List<ProductVariant> findByProductId(String productId);
    Optional<ProductVariant> findBySku(String sku);

    Optional<ProductVariant> findById(String id);

    List<ProductVariant> findTopByStatusOrderBySoldQtyDesc(com.chuadatten.product.common.Status status, org.springframework.data.domain.Pageable pageable);
}
