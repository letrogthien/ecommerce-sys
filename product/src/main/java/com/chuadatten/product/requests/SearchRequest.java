package com.chuadatten.product.requests;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Sort;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for search functionality
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequest {
    
    /**
     * Search keyword for text search
     */
    private String keyword;
    
    /**
     * Categories to filter by
     */
    private List<String> categoryIds;
    
    /**
     * Minimum price filter
     */
    private BigDecimal minPrice;
    
    /**
     * Maximum price filter
     */
    private BigDecimal maxPrice;
    
    /**
     * Tags to filter by
     */
    private List<String> tags;
    
    /**
     * Product attributes for filtering (e.g., color, brand, etc.)
     */
    private java.util.Map<String, String> attributes;
    
    /**
     * Minimum rating filter
     */
    private Double minRating;
    
    /**
     * Maximum rating filter
     */
    private Double maxRating;
    
    /**
     * Sort field
     */
    @Builder.Default
    private String sortBy = "relevance"; // relevance, price, rating, createdAt
    
    /**
     * Sort direction
     */
    @Builder.Default
    private Sort.Direction sortDirection = Sort.Direction.DESC;
    
    /**
     * Page number (0-based)
     */
    @Builder.Default
    private int page = 0;
    
    /**
     * Page size
     */
    @Builder.Default
    private int size = 20;
    
    /**
     * Include inactive/soft-deleted items (admin only)
     */
    @Builder.Default
    private boolean includeInactive = false;
}