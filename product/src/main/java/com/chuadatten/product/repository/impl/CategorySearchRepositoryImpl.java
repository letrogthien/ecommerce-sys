package com.chuadatten.product.repository.impl;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.TextQuery;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.chuadatten.product.common.Status;
import com.chuadatten.product.dto.SearchResultDto;
import com.chuadatten.product.entity.Category;
import com.chuadatten.product.repository.CategorySearchRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of CategorySearchRepository using MongoDB text search and aggregation
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class CategorySearchRepositoryImpl implements CategorySearchRepository {

    private final MongoTemplate mongoTemplate;
    
    private static final String STATUS_FIELD = "status";

    @Override
    public SearchResultDto<Category> searchCategories(String keyword, int page, int size, boolean includeInactive) {
        log.debug("Searching categories with keyword: {}, page: {}, size: {}", keyword, page, size);
        long startTime = System.currentTimeMillis();

        try {
            // Build query using simple Query approach instead of aggregation
            org.springframework.data.mongodb.core.query.Query query = buildCategoryQuery(keyword, includeInactive);
            
            // Get total count first
            long totalCount = mongoTemplate.count(query, Category.class);
            
            // Add pagination and sorting
            query.skip((long) page * size).limit(size);
            
            // Execute search
            List<Category> categories = mongoTemplate.find(query, Category.class);
            
            // Calculate pagination info
            int totalPages = (int) Math.ceil((double) totalCount / size);
            
            long executionTime = System.currentTimeMillis() - startTime;
            log.debug("Category search completed in {} ms, found {} categories", executionTime, categories.size());
            
            return SearchResultDto.<Category>builder()
                    .items(categories)
                    .totalItems(totalCount)
                    .currentPage(page)
                    .pageSize(size)
                    .totalPages(totalPages)
                    .executionTimeMs(executionTime)
                    .build();
                    
        } catch (Exception e) {
            log.error("Error searching categories", e);
            return SearchResultDto.<Category>builder()
                    .items(Collections.emptyList())
                    .totalItems(0)
                    .currentPage(page)
                    .pageSize(size)
                    .totalPages(0)
                    .executionTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    @Override
    public List<String> getCategorySuggestions(String partial, int limit) {
        if (!StringUtils.hasText(partial)) {
            return Collections.emptyList();
        }
        
        try {
            // Use regex for partial matching on category name
            Criteria criteria = Criteria.where("name")
                    .regex("^" + Pattern.quote(partial), "i")
                    .and(STATUS_FIELD).is(Status.ACTIVE);
            
            List<AggregationOperation> pipeline = List.of(
                    Aggregation.match(criteria),
                    Aggregation.project("name"),
                    Aggregation.sort(org.springframework.data.domain.Sort.by("name")),
                    Aggregation.limit(limit)
            );
            
            Aggregation aggregation = Aggregation.newAggregation(pipeline);
            List<Category> categories = mongoTemplate.aggregate(aggregation, "categories", Category.class)
                    .getMappedResults();
            
            return categories.stream()
                    .map(Category::getName)
                    .toList();
                    
        } catch (Exception e) {
            log.error("Error getting category suggestions for partial: {}", partial, e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<Category> findByPathPattern(String pathPattern) {
        if (!StringUtils.hasText(pathPattern)) {
            return Collections.emptyList();
        }
        
        try {
            Criteria criteria = Criteria.where("path")
                    .regex(pathPattern, "i")
                    .and(STATUS_FIELD).is(Status.ACTIVE);
            
            List<AggregationOperation> pipeline = List.of(
                    Aggregation.match(criteria),
                    Aggregation.sort(org.springframework.data.domain.Sort.by("path"))
            );
            
            Aggregation aggregation = Aggregation.newAggregation(pipeline);
            return mongoTemplate.aggregate(aggregation, "categories", Category.class)
                    .getMappedResults();
                    
        } catch (Exception e) {
            log.error("Error finding categories by path pattern: {}", pathPattern, e);
            return Collections.emptyList();
        }
    }

    private org.springframework.data.mongodb.core.query.Query buildCategoryQuery(String keyword, boolean includeInactive) {
        org.springframework.data.mongodb.core.query.Query query;
        
        if (StringUtils.hasText(keyword)) {
            // Use text search
            TextCriteria textCriteria = TextCriteria.forDefaultLanguage()
                    .matchingAny(keyword.split("\\s+"));
            query = TextQuery.queryText(textCriteria).sortByScore();
        } else {
            // Use regular query
            query = new org.springframework.data.mongodb.core.query.Query();
        }
        
        // Add status filter
        if (!includeInactive) {
            query.addCriteria(Criteria.where(STATUS_FIELD).is(Status.ACTIVE));
        }
        
        // Add default sorting if not text search
        if (!StringUtils.hasText(keyword)) {
            query.with(org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Direction.ASC, "sortOrder"));
        }
        
        return query;
    }
}