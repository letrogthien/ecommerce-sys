package com.chuadatten.user.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerApplicationReviewRequest {
    private String action; // APPROVE or REJECT
    private String comment;
}