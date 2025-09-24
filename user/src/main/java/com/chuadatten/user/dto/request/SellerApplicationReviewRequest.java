package com.chuadatten.user.dto.request;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerApplicationReviewRequest {
    private String action; // APPROVE or REJECT
    private String comment;
}