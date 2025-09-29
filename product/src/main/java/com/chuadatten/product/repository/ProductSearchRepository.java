package com.chuadatten.product.repository;

import com.chuadatten.product.dto.SearchFacetsDto;
import com.chuadatten.product.dto.SearchResultDto;
import com.chuadatten.product.entity.Product;
import com.chuadatten.product.requests.SearchRequest;

/**
 * Custom repository interface for advanced product search operations
 */
public interface ProductSearchRepository {
    
    /**
     * Search products using text index and aggregation pipeline
     * 
     * @param request search criteria
     * @return search results with pagination and facets
     */
    SearchResultDto<Product> searchProducts(SearchRequest request);
    
    /**
     * Get search facets for filtering options
     * 
     * @param request base search criteria to compute facets from
     * @return facets with counts
     */
    SearchFacetsDto getSearchFacets(SearchRequest request);
    
    /**
     * Get product suggestions based on partial text
     * 
     * @param partial partial text for suggestions
     * @param limit maximum number of suggestions
     * @return list of suggested product names
     */
    java.util.List<String> getProductSuggestions(String partial, int limit);
}