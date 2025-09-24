package com.chuadatten.user.dto.request;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KycRejectRequest {
    private String reason;
    private String comment;
}