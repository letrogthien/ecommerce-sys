package com.chuadatten.product.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.chuadatten.product.common.MyUtil;
import com.chuadatten.product.common.Status;
import com.chuadatten.product.dto.ProductDto;
import com.chuadatten.product.entity.Product;
import com.chuadatten.product.entity.ProductImage;
import com.chuadatten.product.entity.ProductVariant;
import com.chuadatten.product.exceptions.CustomException;
import com.chuadatten.product.exceptions.ErrorCode;
import com.chuadatten.product.file.FileStorageService;
import com.chuadatten.product.mapper.ProductMapper;
import com.chuadatten.product.repository.ProductRepository;
import com.chuadatten.product.repository.ProductVariantRepository;
import com.chuadatten.product.requests.ProductCreateRq;
import com.chuadatten.product.requests.ProductUpdateRq;
import com.chuadatten.product.responses.ApiResponse;
import com.chuadatten.product.service.ProductService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductMapper productMapper;
    private final FileStorageService fileStorageService;

    @Override
    public ApiResponse<ProductDto> create(ProductCreateRq rq, String userId) {
        Map<String, Object> map = createNewProduct(rq, userId);

        return ApiResponse.<ProductDto>builder()
                .data(productMapper.toDto((Product) map.get("product")))
                .build();
    }

    private Map<String, Object> createNewProduct(ProductCreateRq rq, String userId) {
        Map<String, Object> map = new HashMap<>();
        String slug = MyUtil.toSlug(rq.getName());
        productRepository.findBySlug(slug).ifPresent(product -> {
            throw new CustomException(ErrorCode.SLUG_EXIST);
        });
        Product product = Product.builder()
                .name(rq.getName())
                .description(rq.getDescription())
                .categoryIds(rq.getCategoryIds())
                .attributes(rq.getAttributesProduct())
                .basePrice(rq.getBasePrice())
                .tags(rq.getTags())
                .active(Status.ACTIVE)
                .currency("VND")
                .slug(slug)
                .userId(userId)
                .build();

        productRepository.save(product);

        ProductVariant productVariant = ProductVariant.builder()
                .productId(product.getId())
                .attributes(rq.getAttributesVariant())
                .attributesHash(rq.getAttributesHash())
                .price(rq.getPrice())
                .availableQty(rq.getAvailableQty())
                .status(Status.ACTIVE)
                .reservedQty(0)
                .soldQty(0)
                .sku(null)
                .build();

        productVariantRepository.save(productVariant);

        map.put("product", product);

        map.put("productVariant", productVariant);

        return map;
    }

    @Override
    public ApiResponse<ProductDto> update(String id, ProductUpdateRq rq, String userId) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
        if (!Objects.equals(product.getUserId(), userId)) {
            throw new CustomException(ErrorCode.U_NOT_HAVE_PERMISSION);
        }
        productMapper.update(rq, product);
        productRepository.save(product);
        return ApiResponse.<ProductDto>builder()
                .data(productMapper.toDto(product))
                .build();
    }

    @Override
    public ApiResponse<Void> delete(String id, String userId) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
        if (!Objects.equals(product.getUserId(), userId)) {
            throw new CustomException(ErrorCode.U_NOT_HAVE_PERMISSION);
        }
        product.setActive(Status.INACTIVE);
        productRepository.save(product);
        return ApiResponse.<Void>builder()
                .build();
    }

    @Override
    public ApiResponse<ProductDto> getById(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
        return ApiResponse.<ProductDto>builder()
                .data(productMapper.toDto(product))
                .build();
    }

    @Override
    public ApiResponse<ProductDto> getBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
        return ApiResponse.<ProductDto>builder()
                .data(productMapper.toDto(product))
                .build();
    }

    @Override
    public ApiResponse<Page<ProductDto>> listByCategory(String categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDto> products = productRepository.findAllByCategoryIdsContaining(categoryId, pageable)
                .map(productMapper::toDto);
        return ApiResponse.<Page<ProductDto>>builder()
                .data(products)
                .build();
    }

    @Override
    public ApiResponse<Page<ProductDto>> search(String keyword, int page, int size) {
        return null;
    }

    @Override
    public ApiResponse<ProductDto> addImages(String productId, MultipartFile file, String alt, boolean main,
            int position) {
        String url = fileStorageService.storeFile(file, "products", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        ProductImage productImage = ProductImage.builder()
                .alt(alt)
                .main(main)
                .position(position)
                .url(url)
                .productId(productId)
                .url(url)
                .build();
        List<ProductImage> pImage = product.getImages() != null ? product.getImages() : new ArrayList<>();
        pImage.add(productImage);
        product.setImages(pImage);
        productRepository.save(product);

        return ApiResponse.<ProductDto>builder()
                .data(productMapper.toDto(product))
                .build();

    }

    @Override
    public ApiResponse<List<ProductDto>> getAll() {
        return ApiResponse.<List<ProductDto>>builder()   
        .data(productMapper.toDtoList(productRepository.findAll()))
        .build();

    }

    @Override
    public ApiResponse<Page<ProductDto>> getAllProducts(int page, int size, String sortBy, String sortDirection) {
        Pageable pageable = PageRequest.of(page, size, 
            sortDirection.equalsIgnoreCase("desc") ? 
            org.springframework.data.domain.Sort.by(sortBy).descending() : 
            org.springframework.data.domain.Sort.by(sortBy).ascending());

        Page<Product> products = productRepository.findAllByActive(Status.ACTIVE, pageable);
        Page<ProductDto> productDtos = products.map(productMapper::toDto);
        
        return ApiResponse.<Page<ProductDto>>builder()
                .data(productDtos)
                .build();
    }

    @Override
    public ApiResponse<Page<ProductDto>> getBySeller(UUID sellerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDto> products = productRepository.findAllByUserId(sellerId.toString(), pageable)
                .map(productMapper::toDto);
        return ApiResponse.<Page<ProductDto>>builder()
                .data(products)
                .build();
    }

}
