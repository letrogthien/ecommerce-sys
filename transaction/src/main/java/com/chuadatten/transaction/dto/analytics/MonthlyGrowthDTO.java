package com.chuadatten.transaction.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyGrowthDTO {
    private Double revenue;
    private Double orders;
    private Double avgOrderValue;
}