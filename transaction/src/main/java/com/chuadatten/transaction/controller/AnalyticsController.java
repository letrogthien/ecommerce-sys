package com.chuadatten.transaction.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.chuadatten.transaction.anotation.JwtClaims;
import com.chuadatten.transaction.dto.analytics.AnalyticsOverviewDTO;
import com.chuadatten.transaction.dto.analytics.CustomerAnalyticsDTO;
import com.chuadatten.transaction.dto.analytics.RevenueChartDataDTO;
import com.chuadatten.transaction.dto.analytics.TopProductDTO;
import com.chuadatten.transaction.responses.ApiResponse;
import com.chuadatten.transaction.service.SellerAnalyticsService;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/transaction-service/analytics")
@RequiredArgsConstructor
public class AnalyticsController {
    
    private final SellerAnalyticsService sellerAnalyticsService;
    
    /**
     * Get comprehensive analytics overview for a seller
     */
    @GetMapping("/seller/{sellerId}/overview")
    public ApiResponse<AnalyticsOverviewDTO> getSellerOverview(
            @Parameter(hidden = true) @JwtClaims("id") UUID currentUserId,
            @PathVariable UUID sellerId,
            @RequestParam(required = false) Integer period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        // TODO: Add authorization check - verify currentUserId is seller or admin
        return sellerAnalyticsService.getSellerOverview(sellerId, period, startDate, endDate);
    }
    
    /**
     * Get revenue chart data for visualization
     */
    @GetMapping("/seller/{sellerId}/revenue-chart")
    public ApiResponse<List<RevenueChartDataDTO>> getRevenueChartData(
            @Parameter(hidden = true) @JwtClaims("id") UUID currentUserId,
            @PathVariable UUID sellerId,
            @RequestParam(defaultValue = "30d") String period,
            @RequestParam(defaultValue = "day") String groupBy) {
        
        // TODO: Add authorization check
        return sellerAnalyticsService.getRevenueChartData(sellerId, period, groupBy);
    }
    
    /**
     * Get top selling products for a seller
     */
    @GetMapping("/seller/{sellerId}/top-products")
    public ApiResponse<List<TopProductDTO>> getTopProducts(
            @Parameter(hidden = true) @JwtClaims("id") UUID currentUserId,
            @PathVariable UUID sellerId,
            @RequestParam(defaultValue = "30") Integer period,
            @RequestParam(defaultValue = "5") Integer limit) {
        
        // TODO: Add authorization check
        return sellerAnalyticsService.getTopProducts(sellerId, period, limit);
    }
    
    /**
     * Get customer analytics for a seller
     */
    @GetMapping("/seller/{sellerId}/customers")
    public ApiResponse<CustomerAnalyticsDTO> getCustomerAnalytics(
            @Parameter(hidden = true) @JwtClaims("id") UUID currentUserId,
            @PathVariable UUID sellerId,
            @RequestParam(defaultValue = "30") Integer period) {
        
        // TODO: Add authorization check
        return sellerAnalyticsService.getCustomerAnalytics(sellerId, period);
    }
    
    /**
     * Get order status distribution
     */
    @GetMapping("/seller/{sellerId}/order-status")
    public ApiResponse<Object> getOrderStatusDistribution(
            @Parameter(hidden = true) @JwtClaims("id") UUID currentUserId,
            @PathVariable UUID sellerId,
            @RequestParam(defaultValue = "30") Integer period) {
        
        // TODO: Add authorization check
        return sellerAnalyticsService.getOrderStatusDistribution(sellerId, period);
    }
    
    /**
     * Get performance metrics for a seller
     */
    @GetMapping("/seller/{sellerId}/performance")
    public ApiResponse<Object> getPerformanceMetrics(
            @Parameter(hidden = true) @JwtClaims("id") UUID currentUserId,
            @PathVariable UUID sellerId,
            @RequestParam(defaultValue = "30") Integer period) {
        
        // TODO: Add authorization check  
        return sellerAnalyticsService.getPerformanceMetrics(sellerId, period);
    }
}