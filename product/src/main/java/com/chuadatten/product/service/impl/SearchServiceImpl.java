package com.chuadatten.product.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.chuadatten.product.dto.CategoryDto;
import com.chuadatten.product.dto.ProductDto;
import com.chuadatten.product.dto.SearchFacetsDto;
import com.chuadatten.product.dto.SearchResultDto;
import com.chuadatten.product.entity.Category;
import com.chuadatten.product.entity.Product;
import com.chuadatten.product.mapper.CategoryMapper;
import com.chuadatten.product.mapper.ProductMapper;
import com.chuadatten.product.repository.CategorySearchRepository;
import com.chuadatten.product.repository.ProductSearchRepository;
import com.chuadatten.product.requests.SearchRequest;
import com.chuadatten.product.responses.ApiResponse;
import com.chuadatten.product.service.SearchService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of SearchService using MongoDB text search and aggregation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SearchServiceImpl implements SearchService {

    private final ProductSearchRepository productSearchRepository;
    private final CategorySearchRepository categorySearchRepository;
    private final ProductMapper productMapper;
    private final CategoryMapper categoryMapper;

    @Override
    public ApiResponse<SearchResultDto<ProductDto>> searchProducts(SearchRequest request) {
        log.info("Searching products with request: {}", request);
        
        try {
            // Validate request
            if (request == null) {
                request = SearchRequest.builder().build();
            }
            
            // Normalize search request
            normalizeSearchRequest(request);
            
            // Execute search
            SearchResultDto<Product> searchResult = productSearchRepository.searchProducts(request);
            
            // Convert to DTOs
            List<ProductDto> productDtos = searchResult.getItems().stream()
                    .map(productMapper::toDto)
                    .collect(Collectors.toList());
            
            SearchResultDto<ProductDto> result = SearchResultDto.<ProductDto>builder()
                    .items(productDtos)
                    .totalItems(searchResult.getTotalItems())
                    .currentPage(searchResult.getCurrentPage())
                    .pageSize(searchResult.getPageSize())
                    .totalPages(searchResult.getTotalPages())
                    .facets(searchResult.getFacets())
                    .executionTimeMs(searchResult.getExecutionTimeMs())
                    .build();
            
            log.info("Product search completed: found {} items in {} ms", 
                    result.getTotalItems(), result.getExecutionTimeMs());
            
            return ApiResponse.<SearchResultDto<ProductDto>>builder()
                    .data(result)
                    .build();
                    
        } catch (Exception e) {
            log.error("Error searching products", e);
            return ApiResponse.<SearchResultDto<ProductDto>>builder()
                    .data(SearchResultDto.<ProductDto>builder()
                            .items(Collections.emptyList())
                            .totalItems(0)
                            .currentPage(request != null ? request.getPage() : 0)
                            .pageSize(request != null ? request.getSize() : 20)
                            .totalPages(0)
                            .executionTimeMs(0)
                            .build())
                    .build();
        }
    }

    @Override
    public ApiResponse<SearchResultDto<CategoryDto>> searchCategories(String keyword, int page, int size, boolean includeInactive) {
        log.info("Searching categories with keyword: '{}', page: {}, size: {}", keyword, page, size);
        
        try {
            // Validate parameters
            page = Math.max(0, page);
            size = Math.min(Math.max(1, size), 100); // Max 100 items per page
            
            // Execute search
            SearchResultDto<Category> searchResult = categorySearchRepository.searchCategories(
                    keyword, page, size, includeInactive);
            
            // Convert to DTOs
            List<CategoryDto> categoryDtos = searchResult.getItems().stream()
                    .map(categoryMapper::toDto)
                    .collect(Collectors.toList());
            
            SearchResultDto<CategoryDto> result = SearchResultDto.<CategoryDto>builder()
                    .items(categoryDtos)
                    .totalItems(searchResult.getTotalItems())
                    .currentPage(searchResult.getCurrentPage())
                    .pageSize(searchResult.getPageSize())
                    .totalPages(searchResult.getTotalPages())
                    .executionTimeMs(searchResult.getExecutionTimeMs())
                    .build();
            
            log.info("Category search completed: found {} items in {} ms", 
                    result.getTotalItems(), result.getExecutionTimeMs());
            
            return ApiResponse.<SearchResultDto<CategoryDto>>builder()
                    .data(result)
                    .build();
                    
        } catch (Exception e) {
            log.error("Error searching categories", e);
            return ApiResponse.<SearchResultDto<CategoryDto>>builder()
                    .data(SearchResultDto.<CategoryDto>builder()
                            .items(Collections.emptyList())
                            .totalItems(0)
                            .currentPage(page)
                            .pageSize(size)
                            .totalPages(0)
                            .executionTimeMs(0)
                            .build())
                    .build();
        }
    }

    @Override
    @Cacheable(value = "searchFacets", key = "#request.toString()")
    public ApiResponse<SearchFacetsDto> getSearchFacets(SearchRequest request) {
        log.debug("Getting search facets for request: {}", request);
        
        try {
            if (request == null) {
                request = SearchRequest.builder().build();
            }
            
            SearchFacetsDto facets = productSearchRepository.getSearchFacets(request);
            
            return ApiResponse.<SearchFacetsDto>builder()
                    .data(facets)
                    .build();
                    
        } catch (Exception e) {
            log.error("Error getting search facets", e);
            return ApiResponse.<SearchFacetsDto>builder()
                    .data(SearchFacetsDto.builder().build())
                    .build();
        }
    }

    @Override
    @Cacheable(value = "productSuggestions", key = "#partial + '_' + #limit")
    public ApiResponse<List<String>> getProductSuggestions(String partial, int limit) {
        log.debug("Getting product suggestions for partial: '{}', limit: {}", partial, limit);
        
        try {
            if (!StringUtils.hasText(partial)) {
                return ApiResponse.<List<String>>builder()
                        .data(Collections.emptyList())
                        .build();
            }
            
            // Validate limit
            limit = Math.min(Math.max(1, limit), 20); // Max 20 suggestions
            
            List<String> suggestions = productSearchRepository.getProductSuggestions(partial.trim(), limit);
            
            return ApiResponse.<List<String>>builder()
                    .data(suggestions)
                    .build();
                    
        } catch (Exception e) {
            log.error("Error getting product suggestions", e);
            return ApiResponse.<List<String>>builder()
                    .data(Collections.emptyList())
                    .build();
        }
    }

    @Override
    @Cacheable(value = "categorySuggestions", key = "#partial + '_' + #limit")
    public ApiResponse<List<String>> getCategorySuggestions(String partial, int limit) {
        log.debug("Getting category suggestions for partial: '{}', limit: {}", partial, limit);
        
        try {
            if (!StringUtils.hasText(partial)) {
                return ApiResponse.<List<String>>builder()
                        .data(Collections.emptyList())
                        .build();
            }
            
            // Validate limit
            limit = Math.min(Math.max(1, limit), 20); // Max 20 suggestions
            
            List<String> suggestions = categorySearchRepository.getCategorySuggestions(partial.trim(), limit);
            
            return ApiResponse.<List<String>>builder()
                    .data(suggestions)
                    .build();
                    
        } catch (Exception e) {
            log.error("Error getting category suggestions", e);
            return ApiResponse.<List<String>>builder()
                    .data(Collections.emptyList())
                    .build();
        }
    }

    @Override
    @Cacheable(value = "popularSearchTerms", key = "#limit")
    public ApiResponse<List<String>> getPopularSearchTerms(int limit) {
        log.debug("Getting popular search terms, limit: {}", limit);
        
        try {
            // This is a placeholder implementation
            // In a real system, you would track search queries and return popular ones
            // For now, return some common gaming-related search terms as examples
            List<String> popularTerms = List.of(
                    "minecraft", "steam", "games", "gaming", 
                    "account", "items", "currency", "skins",
                    "mmorpg", "fps", "battle royale", "rpg"
            );
            
            limit = Math.min(Math.max(1, limit), popularTerms.size());
            
            return ApiResponse.<List<String>>builder()
                    .data(popularTerms.subList(0, limit))
                    .build();
                    
        } catch (Exception e) {
            log.error("Error getting popular search terms", e);
            return ApiResponse.<List<String>>builder()
                    .data(Collections.emptyList())
                    .build();
        }
    }

    /**
     * Normalize and validate search request parameters
     */
    private void normalizeSearchRequest(SearchRequest request) {
        // Normalize keyword
        if (StringUtils.hasText(request.getKeyword())) {
            request.setKeyword(request.getKeyword().trim());
        }
        
        // Validate pagination
        request.setPage(Math.max(0, request.getPage()));
        request.setSize(Math.min(Math.max(1, request.getSize()), 100)); // Max 100 items per page
        
        // Validate sort parameters
        if (!StringUtils.hasText(request.getSortBy())) {
            request.setSortBy("relevance");
        }
        
        if (request.getSortDirection() == null) {
            request.setSortDirection(org.springframework.data.domain.Sort.Direction.DESC);
        }
        
        // Validate price range
        if (request.getMinPrice() != null && request.getMaxPrice() != null) {
            if (request.getMinPrice().compareTo(request.getMaxPrice()) > 0) {
                // Swap if min > max
                var temp = request.getMinPrice();
                request.setMinPrice(request.getMaxPrice());
                request.setMaxPrice(temp);
            }
        }
        
        // Validate rating range
        if (request.getMinRating() != null) {
            request.setMinRating(Math.max(0.0, Math.min(5.0, request.getMinRating())));
        }
        if (request.getMaxRating() != null) {
            request.setMaxRating(Math.max(0.0, Math.min(5.0, request.getMaxRating())));
        }
    }
}