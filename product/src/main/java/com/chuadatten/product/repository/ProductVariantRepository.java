package com.chuadatten.product.repository;

import com.chuadatten.product.entity.ProductVariant;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends MongoRepository<ProductVariant, String> {
    List<ProductVariant> findByProductId(String productId);
    Optional<ProductVariant> findBySku(String sku);

    Optional<ProductVariant> findById(String id);
}
