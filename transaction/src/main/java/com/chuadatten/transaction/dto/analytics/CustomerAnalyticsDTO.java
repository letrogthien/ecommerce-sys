package com.chuadatten.transaction.dto.analytics;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerAnalyticsDTO {
    private Integer totalCustomers;
    private Integer newCustomers;
    private Integer returningCustomers;
    private Double averageOrdersPerCustomer;
    private List<VipCustomerDTO> vipCustomers;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VipCustomerDTO {
        private UUID customerId;
        private String customerName;
        private Integer totalOrders;
        private BigDecimal totalSpent;
        private String customerType; // NEW, RETURNING, VIP
    }
}