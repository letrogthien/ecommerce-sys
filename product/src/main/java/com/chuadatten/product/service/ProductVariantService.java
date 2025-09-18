package com.chuadatten.product.service;

import java.util.List;

import com.chuadatten.product.dto.ProductVariantDto;
import com.chuadatten.product.requests.VariantCreateRq;
import com.chuadatten.product.requests.VariantUpdateRq;
import com.chuadatten.product.responses.ApiResponse;

/**
 * Service interface for managing product variants.
 * Provides CRUD operations and additional variant-specific functionality including stock management.
 */
public interface ProductVariantService {

    /**
     * Creates a new product variant.
     * 
     * @param request the product variant request object containing variant information
     * @param userId the ID of the user performing the operation
     * @return an ApiResponse containing the created product variant DTO
     */
    ApiResponse<ProductVariantDto> create(VariantCreateRq request, String userId);

    /**
     * Updates an existing product variant.
     * 
     * @param id the ID of the product variant to update
     * @param request the product variant request object containing updated information
     * @param userId the ID of the user performing the operation
     * @return an ApiResponse containing the updated product variant DTO
     */
    ApiResponse<ProductVariantDto> update(String id, VariantUpdateRq request, String userId);

    /**
     * Deletes a product variant by its ID.
     * 
     * @param id the ID of the product variant to delete
     * @param userId the ID of the user performing the operation
     * @return an ApiResponse indicating the success or failure of the operation
     */
    ApiResponse<Void> delete(String id, String userId);

    /**
     * Retrieves a product variant by its ID.
     * 
     * @param id the ID of the product variant to retrieve
     * @return an ApiResponse containing the requested product variant DTO
     */
    ApiResponse<ProductVariantDto> getById(String id);

    /**
     * Retrieves all product variants for a specific product.
     * 
     * @param productId the ID of the product
     * @return an ApiResponse containing a list of product variant DTOs
     */
    ApiResponse<List<ProductVariantDto>> listByProduct(String productId);

    // Stock operations

    /**
     * Reserves a specified quantity of a product variant.
     * This operation typically occurs when items are added to a cart but not yet purchased.
     * 
     * @param variantId the ID of the product variant
     * @param qty the quantity to reserve
     * @return an ApiResponse containing the updated product variant DTO
     */
    ApiResponse<ProductVariantDto> reserve(String variantId, int qty);

    /**
     * Commits a previously reserved quantity of a product variant.
     * This operation typically occurs when a purchase is completed.
     * 
     * @param variantId the ID of the product variant
     * @param qty the quantity to commit
     * @return an ApiResponse containing the updated product variant DTO
     */
    ApiResponse<ProductVariantDto> commit(String variantId, int qty);

    /**
     * Releases a previously reserved quantity of a product variant.
     * This operation typically occurs when items are removed from a cart or a purchase is canceled.
     * 
     * @param variantId the ID of the product variant
     * @param qty the quantity to release
     * @return an ApiResponse containing the updated product variant DTO
     */
    ApiResponse<ProductVariantDto> release(String variantId, int qty);



    ApiResponse<List<ProductVariantDto>> getTopSold(int limit);
}
