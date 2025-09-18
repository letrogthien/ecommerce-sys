package com.chuadatten.product.controller;

import com.chuadatten.product.dto.CategoryDto;
import com.chuadatten.product.responses.ApiResponse;
import com.chuadatten.product.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/api/v1/product-service/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/{id}")
    public ApiResponse<CategoryDto> getById(@PathVariable String id) {
        return categoryService.getById(id);
    }

    @GetMapping("/slug/{slug}")
    public ApiResponse<CategoryDto> getBySlug(@PathVariable String slug) {
        return categoryService.getBySlug(slug);
    }

    @GetMapping("/{parentId}/children")
    public ApiResponse<List<CategoryDto>> getChildren(@PathVariable String parentId) {
        return categoryService.getChildren(parentId);
    }

    @GetMapping("/root")
    public ApiResponse<List<CategoryDto>> getRoot() {
        return categoryService.getRoot();
    }
    
}