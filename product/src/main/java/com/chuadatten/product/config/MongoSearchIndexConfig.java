package com.chuadatten.product.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.TextIndexDefinition;
import org.springframework.stereotype.Component;

import com.chuadatten.product.entity.Category;
import com.chuadatten.product.entity.Product;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * MongoDB index configuration for search functionality
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MongoSearchIndexConfig implements CommandLineRunner {

    private final MongoTemplate mongoTemplate;

    @Override
    public void run(String... args) throws Exception {
        createProductTextIndex();
        createCategoryTextIndex();
        createAdditionalIndexes();
    }

    /**
     * Create text index for Product collection
     */
    private void createProductTextIndex() {
        try {
            log.info("Creating text index for Product collection...");
            
            TextIndexDefinition textIndex = new TextIndexDefinition.TextIndexDefinitionBuilder()
                    .onField("name", 10f)      // Weight 10 for name field
                    .onField("description", 3f) // Weight 3 for description field
                    .onField("tags", 2f)       // Weight 2 for tags
                    .named("product_text_index")
                    .build();
            
            mongoTemplate.indexOps(Product.class).createIndex(textIndex);
            log.info("Product text index created successfully");
            
        } catch (Exception e) {
            log.error("Error creating product text index", e);
        }
    }

    /**
     * Create text index for Category collection
     */
    private void createCategoryTextIndex() {
        try {
            log.info("Creating text index for Category collection...");
            
            TextIndexDefinition textIndex = new TextIndexDefinition.TextIndexDefinitionBuilder()
                    .onField("name", 10f)       // Weight 10 for name field
                    .onField("description", 5f) // Weight 5 for description field
                    .onField("path", 2f)        // Weight 2 for path field
                    .named("category_text_index")
                    .build();
            
            mongoTemplate.indexOps(Category.class).createIndex(textIndex);
            log.info("Category text index created successfully");
            
        } catch (Exception e) {
            log.error("Error creating category text index", e);
        }
    }

    /**
     * Create additional indexes for search performance
     */
    private void createAdditionalIndexes() {
        try {
            log.info("Creating additional search indexes...");
            
            // Product indexes for filtering
            mongoTemplate.indexOps(Product.class).createIndex(
                    new Index().on("active", org.springframework.data.domain.Sort.Direction.ASC)
                            .on("softDeletedAt", org.springframework.data.domain.Sort.Direction.ASC)
                            .named("product_active_index"));
            
            mongoTemplate.indexOps(Product.class).createIndex(
                    new Index().on("categoryIds", org.springframework.data.domain.Sort.Direction.ASC)
                            .named("product_category_index"));
            
            mongoTemplate.indexOps(Product.class).createIndex(
                    new Index().on("basePrice", org.springframework.data.domain.Sort.Direction.ASC)
                            .named("product_price_index"));
            
            mongoTemplate.indexOps(Product.class).createIndex(
                    new Index().on("ratingAvg", org.springframework.data.domain.Sort.Direction.DESC)
                            .named("product_rating_index"));
            
            mongoTemplate.indexOps(Product.class).createIndex(
                    new Index().on("tags", org.springframework.data.domain.Sort.Direction.ASC)
                            .named("product_tags_index"));
            
            // Compound index for common queries
            mongoTemplate.indexOps(Product.class).createIndex(
                    new Index().on("active", org.springframework.data.domain.Sort.Direction.ASC)
                            .on("categoryIds", org.springframework.data.domain.Sort.Direction.ASC)
                            .on("basePrice", org.springframework.data.domain.Sort.Direction.ASC)
                            .named("product_filter_compound_index"));
            
            // Category indexes
            mongoTemplate.indexOps(Category.class).createIndex(
                    new Index().on("status", org.springframework.data.domain.Sort.Direction.ASC)
                            .on("sortOrder", org.springframework.data.domain.Sort.Direction.ASC)
                            .named("category_status_sort_index"));
            
            mongoTemplate.indexOps(Category.class).createIndex(
                    new Index().on("parentId", org.springframework.data.domain.Sort.Direction.ASC)
                            .on("sortOrder", org.springframework.data.domain.Sort.Direction.ASC)
                            .named("category_hierarchy_index"));
            
            mongoTemplate.indexOps(Category.class).createIndex(
                    new Index().on("path", org.springframework.data.domain.Sort.Direction.ASC)
                            .named("category_path_index"));
            
            log.info("Additional search indexes created successfully");
            
        } catch (Exception e) {
            log.error("Error creating additional search indexes", e);
        }
    }
}