package com.chuadatten.product.service;

import java.util.List;

import com.chuadatten.product.dto.CategoryDto;
import com.chuadatten.product.dto.ProductDto;
import com.chuadatten.product.dto.SearchFacetsDto;
import com.chuadatten.product.dto.SearchResultDto;
import com.chuadatten.product.requests.SearchRequest;
import com.chuadatten.product.responses.ApiResponse;

/**
 * Service interface for search functionality
 */
public interface SearchService {
    
    /**
     * Search products with advanced filtering and pagination
     * 
     * @param request search criteria
     * @return search results with products
     */
    ApiResponse<SearchResultDto<ProductDto>> searchProducts(SearchRequest request);
    
    /**
     * Search categories by keyword
     * 
     * @param keyword search keyword
     * @param page page number (0-based)
     * @param size page size
     * @param includeInactive include inactive categories
     * @return search results with categories
     */
    ApiResponse<SearchResultDto<CategoryDto>> searchCategories(String keyword, int page, int size, boolean includeInactive);
    
    /**
     * Get search facets for advanced filtering
     * 
     * @param request base search criteria
     * @return facets with counts
     */
    ApiResponse<SearchFacetsDto> getSearchFacets(SearchRequest request);
    
    /**
     * Get product suggestions for autocomplete
     * 
     * @param partial partial text input
     * @param limit maximum number of suggestions
     * @return list of product name suggestions
     */
    ApiResponse<List<String>> getProductSuggestions(String partial, int limit);
    
    /**
     * Get category suggestions for autocomplete
     * 
     * @param partial partial text input
     * @param limit maximum number of suggestions
     * @return list of category name suggestions
     */
    ApiResponse<List<String>> getCategorySuggestions(String partial, int limit);
    
    /**
     * Get popular search terms
     * 
     * @param limit maximum number of terms
     * @return list of popular search terms
     */
    ApiResponse<List<String>> getPopularSearchTerms(int limit);
}