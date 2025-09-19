package com.chuadatten.product.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.chuadatten.product.anotation.JwtClaims;
import com.chuadatten.product.dto.ProductDto;
import com.chuadatten.product.requests.ProductCreateRq;
import com.chuadatten.product.requests.ProductUpdateRq;
import com.chuadatten.product.responses.ApiResponse;
import com.chuadatten.product.service.ProductService;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/v1/product-service/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ApiResponse<Page<ProductDto>> getAllProducts(@RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "10") int size,
                                                       @RequestParam(defaultValue = "createdAt") String sortBy,
                                                       @RequestParam(defaultValue = "desc") String sortDirection) {
        return productService.getAllProducts(page, size, sortBy, sortDirection);
    }

    @PostMapping
    public ApiResponse<ProductDto> create(@RequestBody ProductCreateRq request,
                                          @Parameter(hidden = true) @JwtClaims("id") UUID userId) {
        return productService.create(request, userId.toString());
    }

    @PutMapping("/{id}")
    public ApiResponse<ProductDto> update(@PathVariable String id,
                                          @RequestBody ProductUpdateRq request,
                                          @Parameter(hidden = true) @JwtClaims("id") UUID userId) {
        return productService.update(id, request, userId.toString());
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id,
                                    @Parameter(hidden = true) @JwtClaims("id") UUID userId) {
        return productService.delete(id, userId.toString());
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductDto> getById(@PathVariable String id) {
        return productService.getById(id);
    }

    @GetMapping("/slug/{slug}")
    public ApiResponse<ProductDto> getBySlug(@PathVariable String slug) {
        return productService.getBySlug(slug);
    }

    @GetMapping("/category/{categoryId}")
    public ApiResponse<Page<ProductDto>> listByCategory(@PathVariable String categoryId,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "10") int size) {
        return productService.listByCategory(categoryId, page, size);
    }

    @GetMapping("/search")
    public ApiResponse<Page<ProductDto>> search(@RequestParam String keyword,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        return productService.search(keyword, page, size);
    }

    @PostMapping("/{productId}/images")
    public ApiResponse<ProductDto> addImages(@PathVariable String productId,
                                             @RequestParam MultipartFile file,
                                             @RequestParam(required = false) String alt,
                                             @RequestParam(defaultValue = "false") boolean main,
                                             @RequestParam(defaultValue = "0") int position,
                                             @Parameter(hidden = true) @JwtClaims("id") UUID userId) {
        return productService.addImages(productId, file, alt, main, position);
    }


    
}