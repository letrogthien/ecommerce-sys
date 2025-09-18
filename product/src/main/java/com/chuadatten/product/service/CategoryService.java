package com.chuadatten.product.service;

import com.chuadatten.product.dto.CategoryDto;
import com.chuadatten.product.responses.ApiResponse;

import java.util.List;

/**
 * Service interface for managing product categories.
 * Provides CRUD operations and additional category-specific functionality.
 */
public interface CategoryService {
    
    
    ApiResponse<CategoryDto> getById(String id);
    
    /**
     * Retrieves a category by its slug.
     * 
     * @param slug the slug of the category to retrieve
     * @return an ApiResponse containing the requested category DTO
     */
    ApiResponse<CategoryDto> getBySlug(String slug);
    
    /**
     * Retrieves all child categories of a parent category.
     * 
     * @param parentId the ID of the parent category
     * @return an ApiResponse containing a list of child category DTOs
     */
    ApiResponse<List<CategoryDto>> getChildren(String parentId);

    ApiResponse<List<CategoryDto>> getRoot();
    

}
