package com.chuadatten.user.dto.request;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KycReviewRequest {
    private String comment;
}