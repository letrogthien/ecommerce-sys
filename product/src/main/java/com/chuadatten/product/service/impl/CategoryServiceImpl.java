package com.chuadatten.product.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.chuadatten.product.dto.CategoryDto;
import com.chuadatten.product.entity.Category;
import com.chuadatten.product.exceptions.CustomException;
import com.chuadatten.product.exceptions.ErrorCode;
import com.chuadatten.product.mapper.CategoryMapper;
import com.chuadatten.product.repository.CategoryRepository;
import com.chuadatten.product.responses.ApiResponse;
import com.chuadatten.product.service.CategoryService;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public ApiResponse<CategoryDto> getById(String id) {
        CategoryDto categoryDto = categoryMapper.toDto(categoryRepository.findById(id).orElseThrow(
            () -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND)
        ));
        return ApiResponse.<CategoryDto>builder()
        .data(categoryDto)
        .build();
    }

    @Override
    public ApiResponse<CategoryDto> getBySlug(String slug) {
        CategoryDto categoryDto = categoryMapper.toDto(categoryRepository.findBySlug(slug).orElseThrow(
            () -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND)
        ));
        return ApiResponse.<CategoryDto>builder()
        .data(categoryDto)
        .build();
    }

    @Override
    public ApiResponse<List<CategoryDto>> getChildren(String parentId) {
        List<Category> category = categoryRepository.findByParentId(parentId);
        return ApiResponse.<List<CategoryDto>>builder()
        .data(categoryMapper.toDtoList(category))
        .build();
    }

    @Override
    public ApiResponse<List<CategoryDto>> getRoot() {
        List<Category> categories = categoryRepository.findByParentIdIsNullOrderBySortOrderAsc()
                .stream()
                .toList();
        return ApiResponse.<List<CategoryDto>>builder()
                .data(categoryMapper.toDtoList(categories))
                .build();
    }



    
}
