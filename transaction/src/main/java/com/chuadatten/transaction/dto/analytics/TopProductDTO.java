package com.chuadatten.transaction.dto.analytics;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopProductDTO {
    private String productId;
    private String productName;
    private Integer totalSales;
    private BigDecimal totalRevenue;
    private Integer rank;
}