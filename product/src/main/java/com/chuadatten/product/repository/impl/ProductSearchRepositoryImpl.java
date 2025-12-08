package com.chuadatten.product.repository.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.TextQuery;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.chuadatten.product.common.Status;
import com.chuadatten.product.dto.SearchFacetsDto;
import com.chuadatten.product.dto.SearchResultDto;
import com.chuadatten.product.entity.Product;
import com.chuadatten.product.repository.ProductSearchRepository;
import com.chuadatten.product.requests.SearchRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Simple implementation of ProductSearchRepository using MongoDB text search
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class ProductSearchRepositoryImpl implements ProductSearchRepository {

    private final MongoTemplate mongoTemplate;

    @Override
    public SearchResultDto<Product> searchProducts(SearchRequest request) {
        log.debug("Searching products with request: {}", request);
        long startTime = System.currentTimeMillis();

        try {
            Query query = buildSearchQuery(request);
            
            // Get total count
            long totalCount = mongoTemplate.count(query, Product.class);
            
            // Add pagination and sorting
            query.skip((long) request.getPage() * request.getSize())
                 .limit(request.getSize());
            
            // Execute search
            List<Product> products = mongoTemplate.find(query, Product.class);
            
            // Calculate pagination info
            int totalPages = (int) Math.ceil((double) totalCount / request.getSize());
            
            long executionTime = System.currentTimeMillis() - startTime;
            log.debug("Search completed in {} ms, found {} products", executionTime, products.size());
            
            return SearchResultDto.<Product>builder()
                    .items(products)
                    .totalItems(totalCount)
                    .currentPage(request.getPage())
                    .pageSize(request.getSize())
                    .totalPages(totalPages)
                    .executionTimeMs(executionTime)
                    .build();
                    
        } catch (Exception e) {
            log.error("Error searching products", e);
            return createEmptyResult(request);
        }
    }

    @Override
    public SearchFacetsDto getSearchFacets(SearchRequest request) {
        // Simplified implementation - return empty facets for now
        return SearchFacetsDto.builder().build();
    }

    @Override
    public List<String> getProductSuggestions(String partial, int limit) {
        if (!StringUtils.hasText(partial)) {
            return Collections.emptyList();
        }
        
        try {
            Query query = new Query();
            query.addCriteria(Criteria.where("name").regex("^" + partial, "i")
                    .and("active").is(Status.ACTIVE)
                    .and("softDeletedAt").isNull());
            query.fields().include("name");
            query.limit(limit);
            
            List<Product> products = mongoTemplate.find(query, Product.class);
            return products.stream()
                    .map(Product::getName)
                    .toList();
                    
        } catch (Exception e) {
            log.error("Error getting product suggestions for partial: {}", partial, e);
            return Collections.emptyList();
        }
    }

    private Query buildSearchQuery(SearchRequest request) {
        Query query;
        
        if (StringUtils.hasText(request.getKeyword())) {
            // Use text search
            TextCriteria textCriteria = TextCriteria.forDefaultLanguage()
                    .matchingAny(request.getKeyword().split("\\s+"));
            query = TextQuery.queryText(textCriteria).sortByScore();
        } else {
            // Use regular query
            query = new Query();
        }
        
        List<Criteria> criteria = new ArrayList<>();
        
        // Active status filter
        if (!request.isIncludeInactive()) {
            criteria.add(Criteria.where("active").is(Status.ACTIVE));
            criteria.add(Criteria.where("softDeletedAt").isNull());
        }
        
        // Category filter
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            criteria.add(Criteria.where("categoryIds").in(request.getCategoryIds()));
        }
        
        // Price range filter
        if (request.getMinPrice() != null || request.getMaxPrice() != null) {
            Criteria priceCriteria = Criteria.where("basePrice");
            if (request.getMinPrice() != null) {
                priceCriteria = priceCriteria.gte(request.getMinPrice());
            }
            if (request.getMaxPrice() != null) {
                priceCriteria = priceCriteria.lte(request.getMaxPrice());
            }
            criteria.add(priceCriteria);
        }
        
        // Tags filter
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            criteria.add(Criteria.where("tags").in(request.getTags()));
        }
        
        // Rating filter
        if (request.getMinRating() != null || request.getMaxRating() != null) {
            Criteria ratingCriteria = Criteria.where("ratingAvg");
            if (request.getMinRating() != null) {
                ratingCriteria = ratingCriteria.gte(request.getMinRating());
            }
            if (request.getMaxRating() != null) {
                ratingCriteria = ratingCriteria.lte(request.getMaxRating());
            }
            criteria.add(ratingCriteria);
        }
        
        // Add criteria to query
        if (!criteria.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteria.toArray(new Criteria[0])));
        }
        
        // Add sorting if not text search
        if (!StringUtils.hasText(request.getKeyword())) {
            Sort sort = buildSort(request);
            query.with(sort);
        }
        
        return query;
    }

    private Sort buildSort(SearchRequest request) {
        String sortBy = request.getSortBy();
        Sort.Direction direction = request.getSortDirection();
        
        return switch (sortBy.toLowerCase()) {
            case "price" -> Sort.by(direction, "basePrice");
            case "rating" -> Sort.by(direction, "ratingAvg");
            case "name" -> Sort.by(direction, "name");
            case "createdat", "created" -> Sort.by(direction, "createdAt");
            default -> Sort.by(direction, "createdAt");
        };
    }

    private SearchResultDto<Product> createEmptyResult(SearchRequest request) {
        return SearchResultDto.<Product>builder()
                .items(Collections.emptyList())
                .totalItems(0)
                .currentPage(request.getPage())
                .pageSize(request.getSize())
                .totalPages(0)
                .executionTimeMs(0)
                .build();
    }
}