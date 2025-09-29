package com.chuadatten.user.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerRatingDto {
    private UUID id;
    private UUID sellerId;
    private String sellerUsername;
    private UUID buyerId;
    private String buyerUsername;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;

}
