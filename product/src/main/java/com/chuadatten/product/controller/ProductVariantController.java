package com.chuadatten.product.controller;

import com.chuadatten.product.dto.ProductVariantDto;
import com.chuadatten.product.requests.VariantCreateRq;
import com.chuadatten.product.requests.VariantUpdateRq;
import com.chuadatten.product.responses.ApiResponse;
import com.chuadatten.product.service.ProductVariantService;
import com.chuadatten.product.anotation.*;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/v1/product-service/product-variants")
@RequiredArgsConstructor
public class ProductVariantController {

    private final ProductVariantService productVariantService;

    @PostMapping
    public ApiResponse<ProductVariantDto> create(@RequestBody VariantCreateRq dto,
                                                 @Parameter(hidden = true) @JwtClaims("id") UUID userId) {
        return productVariantService.create(dto, userId.toString());
    }

    @PutMapping("/{id}")
    public ApiResponse<ProductVariantDto> update(@PathVariable String id,
                                                 @RequestBody VariantUpdateRq dto,
                                                 @Parameter(hidden = true) @JwtClaims("id") UUID userId) {
        return productVariantService.update(id, dto, userId.toString());
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id,
                                    @Parameter(hidden = true) @JwtClaims("id") UUID userId) {
        return productVariantService.delete(id, userId.toString());
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductVariantDto> getById(@PathVariable String id) {
        return productVariantService.getById(id);
    }

    @GetMapping("/product/{productId}")
    public ApiResponse<List<ProductVariantDto>> listByProduct(@PathVariable String productId) {
        return productVariantService.listByProduct(productId);
    }

    // stock operations
    @PostMapping("/{variantId}/reserve")
    public ApiResponse<ProductVariantDto> reserve(@PathVariable String variantId, @RequestParam int qty) {
        return productVariantService.reserve(variantId, qty);
    }

    @PostMapping("/{variantId}/commit")
    public ApiResponse<ProductVariantDto> commit(@PathVariable String variantId, @RequestParam int qty) {
        return productVariantService.commit(variantId, qty);
    }

    @PostMapping("/{variantId}/release")
    public ApiResponse<ProductVariantDto> release(@PathVariable String variantId, @RequestParam int qty) {
        return productVariantService.release(variantId, qty);
    }

    @GetMapping("/hot-deal")
    public ApiResponse<List<ProductVariantDto>> getHotDeal(@RequestParam int limit) {
        return productVariantService.getTopSold( limit);
    }
    
}