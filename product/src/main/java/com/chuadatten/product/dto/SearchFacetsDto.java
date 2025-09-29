package com.chuadatten.product.dto;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for search facets (aggregated filter options)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchFacetsDto {
    
    /**
     * Available categories with product counts
     */
    private List<FacetItemDto> categories;
    
    /**
     * Price ranges with product counts
     */
    private List<FacetItemDto> priceRanges;
    
    /**
     * Available tags with counts
     */
    private List<FacetItemDto> tags;
    
    /**
     * Available attributes (brand, color, etc.) with counts
     */
    private Map<String, List<FacetItemDto>> attributes;
    
    /**
     * Rating ranges with counts
     */
    private List<FacetItemDto> ratingRanges;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FacetItemDto {
        private String value;
        private String displayName;
        private long count;
    }
}