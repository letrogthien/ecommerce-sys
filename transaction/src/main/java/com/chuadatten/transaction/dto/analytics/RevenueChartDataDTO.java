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
public class RevenueChartDataDTO {
    private String period;
    private BigDecimal revenue;
    private Integer orders;
}