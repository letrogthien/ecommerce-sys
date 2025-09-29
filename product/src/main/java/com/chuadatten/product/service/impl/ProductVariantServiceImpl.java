
package com.chuadatten.product.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.chuadatten.product.common.Status;
import com.chuadatten.product.dto.ProductVariantDto;
import com.chuadatten.product.entity.ProductVariant;
import com.chuadatten.product.exceptions.CustomException;
import com.chuadatten.product.exceptions.ErrorCode;
import com.chuadatten.product.mapper.ProductVariantMapper;
import com.chuadatten.product.repository.ProductVariantRepository;
import com.chuadatten.product.requests.VariantCreateRq;
import com.chuadatten.product.requests.VariantUpdateRq;
import com.chuadatten.product.responses.ApiResponse;
import com.chuadatten.product.service.ProductVariantService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductVariantServiceImpl implements ProductVariantService {
        private final ProductVariantRepository productVariantRepository;
        private final ProductVariantMapper productVariantMapper;
        private final InventoryService inventoryService;

        @Override
        public ApiResponse<ProductVariantDto> create(VariantCreateRq request, String userId) {
                ProductVariant productVariant = ProductVariant.builder()
                                .attributes(request.getAttributes())
                                .attributesHash(request.getAttributesHash())
                                .availableQty(request.getAvailableQty())
                                .reservedQty(0)
                                .soldQty(0)
                                .sku(request.getSku())
                                .price(request.getPrice())
                                .productId(request.getProductId())
                                .status(Status.ACTIVE)
                                .build();
                productVariantRepository.save(productVariant);
                return ApiResponse.<ProductVariantDto>builder()
                                .data(productVariantMapper.toDto(productVariant))
                                .build();
        }

        @Override
        public ApiResponse<ProductVariantDto> update(String id, VariantUpdateRq request, String userId) {
                ProductVariant productVariant = productVariantRepository.findById(id)
                                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND));
                productVariantMapper.update(request, productVariant);
                productVariantRepository.save(productVariant);
                return ApiResponse.<ProductVariantDto>builder()
                                .data(productVariantMapper.toDto(productVariant))
                                .build();
        }

        @Override
        public ApiResponse<Void> delete(String id, String userId) {
                ProductVariant productVariant = productVariantRepository.findById(id)
                                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND));
                productVariantRepository.delete(productVariant);
                return ApiResponse.<Void>builder()
                                .message("delete success")
                                .build();
        }

        @Override
        public ApiResponse<ProductVariantDto> getById(String id) {
                ProductVariant productVariant = productVariantRepository.findById(id)
                                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND));
                return ApiResponse.<ProductVariantDto>builder()
                                .data(productVariantMapper.toDto(productVariant))
                                .build();
        }

        @Override
        public ApiResponse<List<ProductVariantDto>> listByProduct(String productId) {
                List<ProductVariantDto> productVariants = productVariantRepository.findByProductId(productId).stream()
                                .map(
                                                productVariantMapper::toDto)
                                .toList();
                return ApiResponse.<List<ProductVariantDto>>builder()
                                .data(productVariants)
                                .build();
        }

        @Override
        public ApiResponse<ProductVariantDto> reserve(String variantId, int qty) {

                ProductVariant updated = inventoryService.reserve(variantId, qty);
                if (updated == null) {
                        throw new CustomException(ErrorCode.PRODUCT_VARIANT_NOT_ENOUGH_QUANTITY);
                }
                return ApiResponse.<ProductVariantDto>builder()
                                .data(productVariantMapper.toDto(updated))
                                .build();

        }

        @Override
        public ApiResponse<ProductVariantDto> commit(String variantId, int qty) {
                ProductVariant updated = inventoryService.commit(variantId, qty);
                if (updated == null) {
                        throw new CustomException(ErrorCode.PRODUCT_VARIANT_NOT_ENOUGH_QUANTITY);
                }
                return ApiResponse.<ProductVariantDto>builder()
                                .data(productVariantMapper.toDto(updated))
                                .build();

        }

        @Override
        public ApiResponse<ProductVariantDto> release(String variantId, int qty) {
                ProductVariant updated = inventoryService.release(variantId, qty);
                if (updated == null) {
                        throw new CustomException(ErrorCode.PRODUCT_VARIANT_NOT_ENOUGH_QUANTITY);
                }
                return ApiResponse.<ProductVariantDto>builder()
                                .data(productVariantMapper.toDto(updated))
                                .build();

        }



        @Override
        public ApiResponse<List<ProductVariantDto>> getTopSold(int limit) {
                                var page = org.springframework.data.domain.PageRequest.of(0, limit);
                var topVariants = productVariantRepository.findTopByStatusOrderBySoldQtyDesc(Status.ACTIVE, page)
                                .stream().map(productVariantMapper::toDto).toList();
                return ApiResponse.<List<ProductVariantDto>>builder()
                                .data(topVariants)
                                .build();
        }

}
