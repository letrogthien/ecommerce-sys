package com.chuadatten.product.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.chuadatten.product.dto.CategoryDto;
import com.chuadatten.product.dto.ProductDto;
import com.chuadatten.product.dto.SearchFacetsDto;
import com.chuadatten.product.dto.SearchResultDto;
import com.chuadatten.product.requests.SearchRequest;
import com.chuadatten.product.responses.ApiResponse;
import com.chuadatten.product.service.SearchService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller for search operations
 */
@RestController
@RequestMapping("/api/v1/product-service/search")
@RequiredArgsConstructor
@Slf4j
public class SearchController {

    private final SearchService searchService;

    /**
     * Search products with advanced filtering
     * 
     * @param request search request with filters
     * @return search results with products
     */
    @PostMapping("/products")
    public ResponseEntity<ApiResponse<SearchResultDto<ProductDto>>> searchProducts(
            @Valid @RequestBody SearchRequest request) {
        log.info("POST /search/products - keyword: '{}', page: {}, size: {}", 
                request.getKeyword(), request.getPage(), request.getSize());
        
        ApiResponse<SearchResultDto<ProductDto>> response = searchService.searchProducts(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Search products using GET with query parameters (for simple searches)
     * 
     * @param keyword search keyword
     * @param categoryIds category filter
     * @param minPrice minimum price filter
     * @param maxPrice maximum price filter
     * @param tags tags filter
     * @param minRating minimum rating filter
     * @param maxRating maximum rating filter
     * @param sortBy sort field
     * @param sortDirection sort direction
     * @param page page number
     * @param size page size
     * @return search results with products
     */
    @GetMapping("/products")
    public ResponseEntity<ApiResponse<SearchResultDto<ProductDto>>> searchProductsGet(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<String> categoryIds,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Double maxRating,
            @RequestParam(defaultValue = "relevance") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction sortDirection,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Map<String, String> attributes) {
        
        log.info("GET /search/products - keyword: '{}', page: {}, size: {}", keyword, page, size);
        
        SearchRequest request = SearchRequest.builder()
                .keyword(keyword)
                .categoryIds(categoryIds)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .tags(tags)
                .minRating(minRating)
                .maxRating(maxRating)
                .attributes(attributes)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .page(page)
                .size(size)
                .build();
        
        ApiResponse<SearchResultDto<ProductDto>> response = searchService.searchProducts(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Search categories
     * 
     * @param keyword search keyword
     * @param page page number
     * @param size page size
     * @param includeInactive include inactive categories
     * @return search results with categories
     */
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<SearchResultDto<CategoryDto>>> searchCategories(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        
        log.info("GET /search/categories - keyword: '{}', page: {}, size: {}", keyword, page, size);
        
        ApiResponse<SearchResultDto<CategoryDto>> response = searchService.searchCategories(
                keyword, page, size, includeInactive);
        return ResponseEntity.ok(response);
    }

    /**
     * Get search facets for advanced filtering
     * 
     * @param request base search criteria
     * @return facets with counts
     */
    @PostMapping("/facets")
    public ResponseEntity<ApiResponse<SearchFacetsDto>> getSearchFacets(
            @Valid @RequestBody SearchRequest request) {
        
        log.debug("POST /search/facets");
        
        ApiResponse<SearchFacetsDto> response = searchService.getSearchFacets(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get search facets using GET (simplified)
     * 
     * @param keyword base keyword for facets
     * @param categoryIds category filter for facets
     * @return facets with counts
     */
    @GetMapping("/facets")
    public ResponseEntity<ApiResponse<SearchFacetsDto>> getSearchFacetsGet(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<String> categoryIds) {
        
        log.debug("GET /search/facets - keyword: '{}'", keyword);
        
        SearchRequest request = SearchRequest.builder()
                .keyword(keyword)
                .categoryIds(categoryIds)
                .build();
        
        ApiResponse<SearchFacetsDto> response = searchService.getSearchFacets(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get product suggestions for autocomplete
     * 
     * @param q partial query text
     * @param limit maximum number of suggestions
     * @return list of product name suggestions
     */
    @GetMapping("/suggestions/products")
    public ResponseEntity<ApiResponse<List<String>>> getProductSuggestions(
            @RequestParam String q,
            @RequestParam(defaultValue = "10") int limit) {
        
        log.debug("GET /search/suggestions/products - q: '{}', limit: {}", q, limit);
        
        ApiResponse<List<String>> response = searchService.getProductSuggestions(q, limit);
        return ResponseEntity.ok(response);
    }

    /**
     * Get category suggestions for autocomplete
     * 
     * @param q partial query text
     * @param limit maximum number of suggestions
     * @return list of category name suggestions
     */
    @GetMapping("/suggestions/categories")
    public ResponseEntity<ApiResponse<List<String>>> getCategorySuggestions(
            @RequestParam String q,
            @RequestParam(defaultValue = "10") int limit) {
        
        log.debug("GET /search/suggestions/categories - q: '{}', limit: {}", q, limit);
        
        ApiResponse<List<String>> response = searchService.getCategorySuggestions(q, limit);
        return ResponseEntity.ok(response);
    }

    /**
     * Get popular search terms
     * 
     * @param limit maximum number of terms
     * @return list of popular search terms
     */
    @GetMapping("/popular-terms")
    public ResponseEntity<ApiResponse<List<String>>> getPopularSearchTerms(
            @RequestParam(defaultValue = "10") int limit) {
        
        log.debug("GET /search/popular-terms - limit: {}", limit);
        
        ApiResponse<List<String>> response = searchService.getPopularSearchTerms(limit);
        return ResponseEntity.ok(response);
    }
}