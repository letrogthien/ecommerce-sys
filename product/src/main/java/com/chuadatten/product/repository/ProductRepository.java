package com.chuadatten.product.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.chuadatten.product.common.Status;
import com.chuadatten.product.entity.Product;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {
    Optional<Product> findBySlug(String slug);

    List<Product> findByCategoryIdsContainsAndActiveIsTrue(String categoryId);

    Page<Product> findAllByCategoryIdsContaining(String categoryId, Pageable pageable);

    @Query("{ : { : ?0 } }")
    List<Product> searchByText(String keyword);

    Page<Product> findAllByUserId(String string, Pageable pageable);

    Page<Product> findAllByActive(Status active, Pageable pageable);

}
