package com.chuadatten.product.repository;

import java.util.List;

import com.chuadatten.product.dto.SearchResultDto;
import com.chuadatten.product.entity.Category;

/**
 * Custom repository interface for category search operations
 */
public interface CategorySearchRepository {
    
    /**
     * Search categories using text index
     * 
     * @param keyword search keyword
     * @param page page number (0-based)
     * @param size page size
     * @param includeInactive include inactive categories
     * @return search results with pagination
     */
    SearchResultDto<Category> searchCategories(String keyword, int page, int size, boolean includeInactive);
    
    /**
     * Get category suggestions based on partial text
     * 
     * @param partial partial text for suggestions
     * @param limit maximum number of suggestions
     * @return list of suggested category names
     */
    List<String> getCategorySuggestions(String partial, int limit);
    
    /**
     * Find categories by path pattern (for breadcrumb navigation)
     * 
     * @param pathPattern path pattern to match
     * @return matching categories
     */
    List<Category> findByPathPattern(String pathPattern);
}