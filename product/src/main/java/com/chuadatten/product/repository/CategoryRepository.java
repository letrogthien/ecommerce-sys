package com.chuadatten.product.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.chuadatten.product.entity.Category;

@Repository
public interface CategoryRepository extends MongoRepository<Category, String> {
    Optional<Category> findBySlug(String slug);
    List<Category> findByParentId(String parentId);
    
    // Method to find root categories (parentId is null) ordered by sortOrder
    List<Category> findByParentIdIsNullOrderBySortOrderAsc();
    
}
