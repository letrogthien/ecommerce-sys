package com.chuadatten.product.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for search results with pagination and facet information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResultDto<T> {
    
    /**
     * Search results
     */
    private List<T> items;
    
    /**
     * Total number of items matching the search criteria
     */
    private long totalItems;
    
    /**
     * Current page number (0-based)
     */
    private int currentPage;
    
    /**
     * Page size
     */
    private int pageSize;
    
    /**
     * Total number of pages
     */
    private int totalPages;
    
    /**
     * Search facets for filters
     */
    private SearchFacetsDto facets;
    
    /**
     * Search execution time in milliseconds
     */
    private long executionTimeMs;
}