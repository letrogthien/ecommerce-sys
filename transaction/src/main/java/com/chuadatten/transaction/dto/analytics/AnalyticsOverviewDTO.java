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
public class AnalyticsOverviewDTO {
    private BigDecimal totalRevenue;
    private Integer totalOrders;
    private BigDecimal averageOrderValue;
    private Integer completedOrders;
    private Integer pendingOrders;
    private Integer cancelledOrders;
    private Integer newCustomers;
    private Integer returningCustomers;
    private Double averageRating;
    private Double deliverySuccessRate;
    private Double averageProcessingTime;
    private Double responseRate;
    private MonthlyGrowthDTO monthlyGrowth;
}