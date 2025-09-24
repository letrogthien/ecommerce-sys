package com.chuadatten.user.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.chuadatten.user.dto.SellerRatingDto;
import com.chuadatten.user.entity.SellerRating;
import com.chuadatten.user.requests.SellerRatingRequest;

@Mapper(componentModel = "spring")
public interface SellerRatingMapper {
    @Mapping(target = "buyerId", source = "buyer.id")
    @Mapping(target = "sellerId", source = "seller.id")
    @Mapping(target = "buyerUsername", source = "buyer.displayName")
    @Mapping(target = "sellerUsername", source = "seller.displayName")
    @Mapping(target = "rating", source = "ratingScore")
    @Mapping(target = "comment", source = "reviewText")
    SellerRatingDto toDto(SellerRating sellerRating);

    @Mapping(target = "buyer", ignore = true)
    @Mapping(target = "seller", ignore = true)
    @Mapping(target = "transaction", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)

    SellerRating toEntity(SellerRatingRequest sellerRatingRequest);

    @Mapping(target = "buyer", ignore = true)
    @Mapping(target = "seller", ignore = true)
    @Mapping(target = "transaction", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(SellerRatingRequest sellerRatingRequest, @MappingTarget SellerRating sellerRating);
}
