package com.chuadatten.product.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import com.chuadatten.product.dto.ProductDto;
import com.chuadatten.product.requests.ProductCreateRq;
import com.chuadatten.product.requests.ProductUpdateRq;
import com.chuadatten.product.responses.ApiResponse;

/**
 * Service interface for managing products.
 * Provides CRUD operations and additional product-specific functionality.
 */
public interface ProductService {

    /**
     * Creates a new product.
     * 
     * @param productCreateRq the product data transfer object containing product information
     * @param userId the ID of the user performing the operation
     * @return an ApiResponse containing the created product DTO
     */
    ApiResponse<ProductDto> create(ProductCreateRq productCreateRq, String userId);

    /**
     * Updates an existing product.
     * 
     * @param id the ID of the product to update
     * @param productUpdateRq the product data transfer object containing updated information
     * @param userId the ID of the user performing the operation
     * @return an ApiResponse containing the updated product DTO
     */
    ApiResponse<ProductDto> update(String id, ProductUpdateRq productUpdateRq,String userId);

    /**
     * Deletes a product by its ID.
     * 
     * @param id the ID of the product to delete
     * @param userId the ID of the user performing the operation
     * @return an ApiResponse indicating the success or failure of the operation
     */
    ApiResponse<Void> delete(String id, String userId);

    /**
     * Retrieves a product by its ID.
     * 
     * @param id the ID of the product to retrieve
     * @return an ApiResponse containing the requested product DTO
     */
    ApiResponse<ProductDto> getById(String id);

    /**
     * Retrieves a product by its slug.
     * 
     * @param slug the slug of the product to retrieve
     * @return an ApiResponse containing the requested product DTO
     */
    ApiResponse<ProductDto> getBySlug(String slug);

    /**
     * Retrieves a paginated list of products belonging to a specific category.
     * 
     * @param categoryId the ID of the category
     * @param page the page number to retrieve
     * @param size the number of items per page
     * @return an ApiResponse containing a page of product DTOs
     */
    ApiResponse<Page<ProductDto>> listByCategory(String categoryId, int page, int size);

    /**
     * Searches for products based on a keyword.
     * 
     * @param keyword the search keyword
     * @param page the page number to retrieve
     * @param size the number of items per page
     * @return an ApiResponse containing a page of matching product DTOs
     */
    ApiResponse<Page<ProductDto>> search(String keyword, int page, int size);


    /**
     * Add images to a product.
     * 
     * @param productId the ID of the product
     * @param images the images to add
     * @param userId the ID of the user
     * @return an ApiResponse indicating the success or failure of the operation
     */
    ApiResponse<ProductDto> addImages(String productId, MultipartFile file, String alt, boolean main, int position);



    ApiResponse<List<ProductDto>> getAll();

    /**
     * Retrieves all products with pagination support.
     * 
     * @param page the page number to retrieve (0-based)
     * @param size the number of items per page
     * @param sortBy the field to sort by
     * @param sortDirection the sort direction (asc or desc)
     * @return an ApiResponse containing a page of product DTOs
     */
    ApiResponse<Page<ProductDto>> getAllProducts(int page, int size, String sortBy, String sortDirection);
}
