package com.chuadatten.transaction.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.chuadatten.transaction.common.Status;
import com.chuadatten.transaction.dto.analytics.AnalyticsOverviewDTO;
import com.chuadatten.transaction.dto.analytics.CustomerAnalyticsDTO;
import com.chuadatten.transaction.dto.analytics.MonthlyGrowthDTO;
import com.chuadatten.transaction.dto.analytics.RevenueChartDataDTO;
import com.chuadatten.transaction.dto.analytics.TopProductDTO;
import com.chuadatten.transaction.exceptions.CustomException;
import com.chuadatten.transaction.exceptions.ErrorCode;
import com.chuadatten.transaction.repository.OrderRepository;
import com.chuadatten.transaction.responses.ApiResponse;
import com.chuadatten.transaction.service.SellerAnalyticsService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SellerAnalyticsServiceImpl implements SellerAnalyticsService {
    
    private final OrderRepository orderRepository;
    
    // Constants for order statuses
    private static final Status STATUS_COMPLETED = Status.COMPLETED;
    private static final Status STATUS_PENDING = Status.PENDING;
    private static final Status STATUS_CANCELLED = Status.CANCELLED;
    private static final Status STATUS_DELIVERED = Status.DELIVERED;
    private static final Status STATUS_PAID = Status.PAID;
    
    @Override
    public ApiResponse<AnalyticsOverviewDTO> getSellerOverview(
            UUID sellerId, 
            Integer period, 
            LocalDate startDate, 
            LocalDate endDate) {
        
        // Validate input parameters
        validateAnalyticsRequest(sellerId, period, startDate, endDate);
        
        // Calculate date range
        LocalDateTime[] dateRange = calculateDateRange(period, startDate, endDate);
        LocalDateTime start = dateRange[0];
        LocalDateTime end = dateRange[1];
        
        // Calculate previous period for growth comparison
        LocalDateTime[] previousPeriod = calculatePreviousPeriod(start, end);
        LocalDateTime previousStart = previousPeriod[0];
        LocalDateTime previousEnd = previousPeriod[1];
        
        try {
            // Get current period metrics
            BigDecimal totalRevenue = orderRepository.calculateRevenueBySeller(sellerId, start, end);
            Integer totalOrders = orderRepository.countOrdersBySeller(sellerId, start, end);

            Integer completedOrders = orderRepository.countOrdersBySellerAndStatus(sellerId, STATUS_DELIVERED, start, end);
            Integer pendingOrders = orderRepository.countOrdersBySellerAndStatus(sellerId, STATUS_PAID, start, end);
            Integer cancelledOrders = orderRepository.countOrdersBySellerAndStatus(sellerId, STATUS_CANCELLED, start, end);
            Integer deliveredOrders = orderRepository.countOrdersBySellerAndStatus(sellerId, STATUS_DELIVERED, start, end);
            // Customer metrics
            Integer totalCustomers = orderRepository.countUniqueCustomersBySeller(sellerId, start, end);
            Integer newCustomers = orderRepository.countNewCustomersBySeller(sellerId, start, end);
            Integer returningCustomers = totalCustomers - newCustomers;
            
            // Calculate average order value
            BigDecimal averageOrderValue = BigDecimal.ZERO;
            if (totalOrders > 0 && totalRevenue != null) {
                averageOrderValue = totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP);
            }
            
            // Calculate previous period metrics for growth
            BigDecimal previousRevenue = orderRepository.calculateRevenueBySeller(sellerId, previousStart, previousEnd);
            Integer previousOrders = orderRepository.countOrdersBySeller(sellerId, previousStart, previousEnd);
            
            // Calculate growth rates
            MonthlyGrowthDTO monthlyGrowth = calculateGrowthRates(
                totalRevenue, totalOrders, averageOrderValue,
                previousRevenue, previousOrders
            );
            
            // Build response
            AnalyticsOverviewDTO overview = AnalyticsOverviewDTO.builder()
                    .totalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
                    .totalOrders(totalOrders)
                    .averageOrderValue(averageOrderValue)
                    .completedOrders(completedOrders)
                    .pendingOrders(pendingOrders)
                    .cancelledOrders(cancelledOrders)
                    .newCustomers(newCustomers)
                    .returningCustomers(returningCustomers)
                    .averageRating(4.8) // Placeholder - would be calculated from ratings when available
                    .deliverySuccessRate(deliveredOrders / (double) completedOrders * 100) // Placeholder - would be calculated from delivery data
                    .averageProcessingTime(2.5) // Placeholder - would be calculated from order processing time
                    .responseRate(98.2) // Placeholder - would be calculated from response metrics
                    .monthlyGrowth(monthlyGrowth)
                    .build();
            
            return ApiResponse.<AnalyticsOverviewDTO>builder()
                    .data(overview)
                    .message("Analytics overview retrieved successfully")
                    .build();
                    
        } catch (Exception e) {
            // Log the actual exception for debugging
            System.err.println("Error in getSellerOverview: " + e.getMessage());
            e.printStackTrace();
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
    
    @Override
    public ApiResponse<List<RevenueChartDataDTO>> getRevenueChartData(
            UUID sellerId, 
            String period, 
            String groupBy) {
        
        // Validate input parameters
        if (sellerId == null) {
            throw new CustomException(ErrorCode.INVALID_PARAMETER);
        }
        
        if (period == null || period.trim().isEmpty()) {
            throw new CustomException(ErrorCode.MISSING_REQUIRED_PARAMETER);
        }
        
        if (groupBy == null || groupBy.trim().isEmpty()) {
            groupBy = "day"; // default groupBy
        }
        
        // Parse period (e.g., "7d", "30d", "90d", "1y")
        LocalDateTime[] dateRange = parsePeriodToDateRange(period);
        LocalDateTime startDate = dateRange[0];
        LocalDateTime endDate = dateRange[1];
        
        try {
            List<Object[]> rawData;
            
            // Get data based on groupBy parameter
            switch (groupBy.toLowerCase()) {
                case "day":
                    rawData = orderRepository.getRevenueChartDataByDay(sellerId, startDate, endDate);
                    break;
                case "week":
                    rawData = orderRepository.getRevenueChartDataByWeek(sellerId, startDate, endDate);
                    break;
                case "month":
                    rawData = orderRepository.getRevenueChartDataByMonth(sellerId, startDate, endDate);
                    break;
                default:
                    throw new CustomException(ErrorCode.INVALID_PARAMETER);
            }
            
            // Convert raw data to DTOs
            List<RevenueChartDataDTO> chartData = rawData.stream()
                    .map(row -> RevenueChartDataDTO.builder()
                            .period((String) row[0])
                            .revenue((BigDecimal) row[1])
                            .orders(((Number) row[2]).intValue())
                            .build())
                    .toList();
            
            return ApiResponse.<List<RevenueChartDataDTO>>builder()
                    .data(chartData)
                    .message("Revenue chart data retrieved successfully")
                    .build();
                    
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
    
    @Override
    public ApiResponse<List<TopProductDTO>> getTopProducts(
            UUID sellerId, 
            Integer period, 
            Integer limit) {
        
        // Validate input parameters
        if (sellerId == null) {
            throw new CustomException(ErrorCode.INVALID_PARAMETER);
        }
        
        if (period == null || period < 1 || period > 365) {
            period = 30; // default to 30 days
        }
        
        if (limit == null || limit < 1 || limit > 100) {
            limit = 10; // default to top 10
        }
        
        // Calculate date range
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(period);
        
        try {
            // Create pageable for limit
            Pageable pageable = PageRequest.of(0, limit);
            
            // Get top products by sales quantity
            List<Object[]> rawData = orderRepository.getTopProductsBySales(sellerId, start, end, pageable);
            
            // Convert to DTOs with ranking
            List<TopProductDTO> topProducts = new ArrayList<>();
            int rank = 1;
            
            for (Object[] row : rawData) {
                String productId = (String) row[0];
                Integer totalSales = ((Number) row[1]).intValue();
                BigDecimal totalRevenue = (BigDecimal) row[2];
                
                TopProductDTO productDTO = TopProductDTO.builder()
                        .productId(productId)
                        .productName("Product " + productId) // Product name would be fetched from product service in real implementation
                        .totalSales(totalSales)
                        .totalRevenue(totalRevenue)
                        .rank(rank++)
                        .build();
                
                topProducts.add(productDTO);
            }
            
            return ApiResponse.<List<TopProductDTO>>builder()
                    .data(topProducts)
                    .message("Top products retrieved successfully")
                    .build();
                    
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
    
    @Override
    public ApiResponse<CustomerAnalyticsDTO> getCustomerAnalytics(
            UUID sellerId, 
            Integer period) {
        
        // Validate input parameters
        if (sellerId == null) {
            throw new CustomException(ErrorCode.INVALID_PARAMETER);
        }
        
        if (period == null || period < 1 || period > 365) {
            period = 30; // default to 30 days
        }
        
        // Calculate date range
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(period);
        
        try {
            // Get basic customer metrics
            Integer totalCustomers = orderRepository.countUniqueCustomersBySeller(sellerId, start, end);
            Integer newCustomers = orderRepository.countNewCustomersBySeller(sellerId, start, end);
            Integer returningCustomers = totalCustomers - newCustomers;
            
            // Get average orders per customer
            Double averageOrdersPerCustomer = orderRepository.getAverageOrdersPerCustomer(sellerId, start, end);
            if (averageOrdersPerCustomer == null) {
                averageOrdersPerCustomer = 0.0;
            }
            
            // Get VIP customers (top 10 by total spent)
            Pageable pageable = PageRequest.of(0, 10);
            List<Object[]> customerData = orderRepository.getCustomerAnalytics(sellerId, start, end, pageable);
            
            List<CustomerAnalyticsDTO.VipCustomerDTO> vipCustomers = new ArrayList<>();
            for (Object[] row : customerData) {
                UUID customerId = (UUID) row[0];
                Integer totalOrders = ((Number) row[1]).intValue();
                BigDecimal totalSpent = (BigDecimal) row[2];
                
                // Determine customer type based on order count and spending
                String customerType;
                if (totalSpent.compareTo(BigDecimal.valueOf(1000)) >= 0) {
                    customerType = "VIP";
                } else if (totalOrders > 1) {
                    customerType = "RETURNING";
                } else {
                    customerType = "NEW";
                }
                
                CustomerAnalyticsDTO.VipCustomerDTO vipCustomer = CustomerAnalyticsDTO.VipCustomerDTO.builder()
                        .customerId(customerId)
                        .customerName("Customer " + customerId.toString().substring(0, 8)) // Customer name would be fetched from user service in real implementation
                        .totalOrders(totalOrders)
                        .totalSpent(totalSpent)
                        .customerType(customerType)
                        .build();
                
                vipCustomers.add(vipCustomer);
            }
            
            // Build response
            CustomerAnalyticsDTO customerAnalytics = CustomerAnalyticsDTO.builder()
                    .totalCustomers(totalCustomers)
                    .newCustomers(newCustomers)
                    .returningCustomers(returningCustomers)
                    .averageOrdersPerCustomer(averageOrdersPerCustomer)
                    .vipCustomers(vipCustomers)
                    .build();
            
            return ApiResponse.<CustomerAnalyticsDTO>builder()
                    .data(customerAnalytics)
                    .message("Customer analytics retrieved successfully")
                    .build();
                    
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
    
    @Override
    public ApiResponse<Object> getOrderStatusDistribution(
            UUID sellerId, 
            Integer period) {
        
        // Validate input parameters
        if (sellerId == null) {
            throw new CustomException(ErrorCode.INVALID_PARAMETER);
        }
        
        if (period == null || period < 1 || period > 365) {
            period = 30; // default to 30 days
        }
        
        // Calculate date range
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(period);
        
        try {
            // Get order counts by status
            Integer completedOrders = orderRepository.countOrdersBySellerAndStatus(sellerId, STATUS_COMPLETED, start, end);
            Integer pendingOrders = orderRepository.countOrdersBySellerAndStatus(sellerId, STATUS_PENDING, start, end);
            Integer cancelledOrders = orderRepository.countOrdersBySellerAndStatus(sellerId, STATUS_CANCELLED, start, end);
            Integer deliveredOrders = orderRepository.countOrdersBySellerAndStatus(sellerId, STATUS_DELIVERED, start, end);
            Integer paidOrders = orderRepository.countOrdersBySellerAndStatus(sellerId, STATUS_PAID, start, end);
            
            // Build response object
            var statusDistribution = new java.util.HashMap<String, Object>();
            statusDistribution.put("completed", completedOrders);
            statusDistribution.put("pending", pendingOrders);
            statusDistribution.put("cancelled", cancelledOrders);
            statusDistribution.put("delivered", deliveredOrders);
            statusDistribution.put("paid", paidOrders);
            
            Integer totalOrders = completedOrders + pendingOrders + cancelledOrders + deliveredOrders + paidOrders;
            statusDistribution.put("total", totalOrders);
            
            // Calculate percentages
            if (totalOrders > 0) {
                statusDistribution.put("completedPercentage", (completedOrders * 100.0) / totalOrders);
                statusDistribution.put("pendingPercentage", (pendingOrders * 100.0) / totalOrders);
                statusDistribution.put("cancelledPercentage", (cancelledOrders * 100.0) / totalOrders);
                statusDistribution.put("deliveredPercentage", (deliveredOrders * 100.0) / totalOrders);
                statusDistribution.put("paidPercentage", (paidOrders * 100.0) / totalOrders);
            }
            
            return ApiResponse.<Object>builder()
                    .data(statusDistribution)
                    .message("Order status distribution retrieved successfully")
                    .build();
                    
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
    
    @Override
    public ApiResponse<Object> getPerformanceMetrics(
            UUID sellerId, 
            Integer period) {
        
        // Validate input parameters
        if (sellerId == null) {
            throw new CustomException(ErrorCode.INVALID_PARAMETER);
        }
        
        if (period == null || period < 1 || period > 365) {
            period = 30; // default to 30 days
        }
        
        // Calculate date range
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(period);
        
        try {
            // Get basic metrics
            BigDecimal totalRevenue = orderRepository.calculateRevenueBySeller(sellerId, start, end);
            Integer totalOrders = orderRepository.countOrdersBySeller(sellerId, start, end);
            Integer completedOrders = orderRepository.countOrdersBySellerAndStatus(sellerId, STATUS_COMPLETED, start, end);
            Integer cancelledOrders = orderRepository.countOrdersBySellerAndStatus(sellerId, STATUS_CANCELLED, start, end);
            
            // Calculate performance metrics
            var performanceMetrics = new java.util.HashMap<String, Object>();
            
            // Revenue metrics
            performanceMetrics.put("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
            performanceMetrics.put("totalOrders", totalOrders);
            
            // Success rate
            Double successRate = 0.0;
            if (totalOrders > 0) {
                successRate = (completedOrders * 100.0) / totalOrders;
            }
            performanceMetrics.put("successRate", successRate);
            
            // Cancellation rate
            Double cancellationRate = 0.0;
            if (totalOrders > 0) {
                cancellationRate = (cancelledOrders * 100.0) / totalOrders;
            }
            performanceMetrics.put("cancellationRate", cancellationRate);
            
            // Average order value
            BigDecimal averageOrderValue = BigDecimal.ZERO;
            if (totalOrders > 0 && totalRevenue != null) {
                averageOrderValue = totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP);
            }
            performanceMetrics.put("averageOrderValue", averageOrderValue);
            
            // Customer metrics
            Integer totalCustomers = orderRepository.countUniqueCustomersBySeller(sellerId, start, end);
            performanceMetrics.put("totalCustomers", totalCustomers);
            
            // Customer lifetime value (simplified)
            BigDecimal customerLifetimeValue = BigDecimal.ZERO;
            if (totalCustomers > 0 && totalRevenue != null) {
                customerLifetimeValue = totalRevenue.divide(BigDecimal.valueOf(totalCustomers), 2, RoundingMode.HALF_UP);
            }
            performanceMetrics.put("customerLifetimeValue", customerLifetimeValue);
            
            return ApiResponse.<Object>builder()
                    .data(performanceMetrics)
                    .message("Performance metrics retrieved successfully")
                    .build();
                    
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
    
    private void validateAnalyticsRequest(UUID sellerId, Integer period, LocalDate startDate, LocalDate endDate) {
        if (sellerId == null) {
            throw new CustomException(ErrorCode.INVALID_PARAMETER);
        }
        
        if (startDate != null && endDate != null) {
            if (startDate.isAfter(endDate)) {
                throw new CustomException(ErrorCode.INVALID_DATE_RANGE);
            }
        } else if (period != null) {
            if (period < 1 || period > 365) {
                throw new CustomException(ErrorCode.INVALID_PARAMETER);
            }
        } else {
            throw new CustomException(ErrorCode.MISSING_REQUIRED_PARAMETER);
        }
    }
    
    private LocalDateTime[] calculateDateRange(Integer period, LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null) {
            return new LocalDateTime[]{
                startDate.atStartOfDay(),
                endDate.atTime(LocalTime.MAX)
            };
        } else {
            LocalDateTime end = LocalDateTime.now();
            LocalDateTime start = end.minusDays(period != null ? period : 30);
            return new LocalDateTime[]{start, end};
        }
    }
    
    private LocalDateTime[] calculatePreviousPeriod(LocalDateTime start, LocalDateTime end) {
        long daysBetween = java.time.Duration.between(start, end).toDays();
        LocalDateTime previousEnd = start.minusDays(1);
        LocalDateTime previousStart = previousEnd.minusDays(daysBetween);
        return new LocalDateTime[]{previousStart, previousEnd};
    }
    
    private MonthlyGrowthDTO calculateGrowthRates(
            BigDecimal currentRevenue, 
            Integer currentOrders, 
            BigDecimal currentAov,
            BigDecimal previousRevenue, 
            Integer previousOrders) {
        
        Double revenueGrowth = calculateGrowthRate(currentRevenue, previousRevenue);
        Double ordersGrowth = calculateGrowthRate(currentOrders, previousOrders);
        
        BigDecimal previousAov = BigDecimal.ZERO;
        if (previousOrders > 0 && previousRevenue != null) {
            previousAov = previousRevenue.divide(BigDecimal.valueOf(previousOrders), 2, RoundingMode.HALF_UP);
        }
        Double aovGrowth = calculateGrowthRate(currentAov, previousAov);
        
        return MonthlyGrowthDTO.builder()
                .revenue(revenueGrowth)
                .orders(ordersGrowth)
                .avgOrderValue(aovGrowth)
                .build();
    }
    
    private Double calculateGrowthRate(Number current, Number previous) {
        if (previous == null || previous.doubleValue() == 0) {
            return current != null && current.doubleValue() > 0 ? 100.0 : 0.0;
        }
        
        if (current == null) {
            return -100.0;
        }
        
        double currentValue = current.doubleValue();
        double previousValue = previous.doubleValue();
        
        return ((currentValue - previousValue) / previousValue) * 100.0;
    }
    
    private LocalDateTime[] parsePeriodToDateRange(String period) {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start;
        
        try {
            // Parse format like "7d", "30d", "90d", "1y", etc.
            String periodLower = period.toLowerCase().trim();
            
            if (periodLower.endsWith("d")) {
                int days = Integer.parseInt(periodLower.substring(0, periodLower.length() - 1));
                start = end.minusDays(days);
            } else if (periodLower.endsWith("w")) {
                int weeks = Integer.parseInt(periodLower.substring(0, periodLower.length() - 1));
                start = end.minusWeeks(weeks);
            } else if (periodLower.endsWith("m")) {
                int months = Integer.parseInt(periodLower.substring(0, periodLower.length() - 1));
                start = end.minusMonths(months);
            } else if (periodLower.endsWith("y")) {
                int years = Integer.parseInt(periodLower.substring(0, periodLower.length() - 1));
                start = end.minusYears(years);
            } else {
                // Try to parse as number of days
                int days = Integer.parseInt(periodLower);
                start = end.minusDays(days);
            }
            
        } catch (NumberFormatException e) {
            throw new CustomException(ErrorCode.INVALID_PARAMETER);
        }
        
        return new LocalDateTime[]{start, end};
    }
}