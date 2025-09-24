package com.chuadatten.transaction.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.chuadatten.transaction.dto.analytics.AnalyticsOverviewDTO;
import com.chuadatten.transaction.dto.analytics.CustomerAnalyticsDTO;
import com.chuadatten.transaction.dto.analytics.RevenueChartDataDTO;
import com.chuadatten.transaction.dto.analytics.TopProductDTO;
import com.chuadatten.transaction.responses.ApiResponse;

public interface SellerAnalyticsService {
    
    /**
     * Get comprehensive analytics overview for a seller
     * @param sellerId The seller's UUID
     * @param period Number of days to look back (7, 30, 90, 365)
     * @param startDate Optional start date (overrides period)
     * @param endDate Optional end date (overrides period)
     * @return Analytics overview with revenue, orders, customers metrics
     */
    ApiResponse<AnalyticsOverviewDTO> getSellerOverview(
            UUID sellerId, 
            Integer period, 
            LocalDate startDate, 
            LocalDate endDate
    );
    
    /**
     * Get revenue chart data for visualization
     * @param sellerId The seller's UUID
     * @param period Period string ("7d", "30d", "3m", "1y")
     * @param groupBy Grouping option ("day", "week", "month")
     * @return Time series data for revenue charts
     */
    ApiResponse<List<RevenueChartDataDTO>> getRevenueChartData(
            UUID sellerId, 
            String period, 
            String groupBy
    );
    
    /**
     * Get top selling products for a seller
     * @param sellerId The seller's UUID
     * @param period Number of days to look back
     * @param limit Maximum number of products to return (default: 5, max: 20)
     * @return List of top performing products ranked by revenue
     */
    ApiResponse<List<TopProductDTO>> getTopProducts(
            UUID sellerId, 
            Integer period, 
            Integer limit
    );
    
    /**
     * Get customer analytics for a seller
     * @param sellerId The seller's UUID
     * @param period Number of days to look back
     * @return Customer statistics including new/returning customers and VIP list
     */
    ApiResponse<CustomerAnalyticsDTO> getCustomerAnalytics(
            UUID sellerId, 
            Integer period
    );
    
    /**
     * Get order status distribution
     * @param sellerId The seller's UUID
     * @param period Number of days to look back
     * @return Order counts and percentages by status
     */
    ApiResponse<Object> getOrderStatusDistribution(
            UUID sellerId, 
            Integer period
    );
    
    /**
     * Get performance metrics for a seller
     * @param sellerId The seller's UUID
     * @param period Number of days to look back
     * @return Performance metrics like delivery success rate, processing time
     */
    ApiResponse<Object> getPerformanceMetrics(
            UUID sellerId, 
            Integer period
    );
}