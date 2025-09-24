
package com.chuadatten.transaction.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.chuadatten.transaction.common.Status;
import com.chuadatten.transaction.entity.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
        Page<Order> findAllBySellerId(UUID sellerId, Pageable pageable);

        Page<Order> findAllByBuyerId(UUID buyerId, Pageable pageable);

        @Query("""
                            SELECT o
                            FROM Order o
                            WHERE (:sellerId IS NULL OR o.sellerId = :sellerId)
                              AND (:buyerId IS NULL OR o.buyerId = :buyerId)
                              AND (:status IS NULL OR o.status = :status)
                              AND (:paymentStatus IS NULL OR o.paymentStatus = :paymentStatus)
                        """)
        Page<Order> findAllOrdersFiltered(
                        @Param("sellerId") UUID sellerId,
                        @Param("buyerId") UUID buyerId,
                        @Param("status") Status status,
                        @Param("paymentStatus") String paymentStatus,
                        Pageable pageable);

        @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :id")
        Optional<Order> findByIdWithItems(@Param("id") UUID id);

        // get order trạng thái order là ready_pay hoặc paying và payment status là
        // pending và createdAt cách hiện tại hơn 30 phút
        @Query("SELECT o FROM Order o WHERE (o.status = 'READY_PAY' OR o.status = 'PAYING') AND ( o.paymentStatus = 'PENDING' OR o.paymentStatus = 'PROCESSING') AND o.createdAt <= :cutoffTime")
        List<Order> findOrdersToCancel(@Param("cutoffTime") LocalDateTime cutoffTime);

        // Analytics queries
        @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.sellerId = :sellerId AND o.status IN ('COMPLETED', 'DELIVERED') AND o.createdAt BETWEEN :startDate AND :endDate")
        BigDecimal calculateRevenueBySeller(@Param("sellerId") UUID sellerId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

        @Query("SELECT COUNT(o) FROM Order o WHERE o.sellerId = :sellerId AND o.createdAt BETWEEN :startDate AND :endDate AND o.status IN ('COMPLETED', 'DELIVERED', 'PAID', 'CANCELLED', 'REFUNDED')")
        Integer countOrdersBySeller(@Param("sellerId") UUID sellerId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

        @Query("SELECT COUNT(o) FROM Order o WHERE o.sellerId = :sellerId AND o.status = :status AND o.createdAt BETWEEN :startDate AND :endDate")
        Integer countOrdersBySellerAndStatus(@Param("sellerId") UUID sellerId, @Param("status") Status status, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

        @Query("SELECT COUNT(DISTINCT o.buyerId) FROM Order o WHERE o.sellerId = :sellerId AND o.createdAt BETWEEN :startDate AND :endDate")
        Integer countUniqueCustomersBySeller(@Param("sellerId") UUID sellerId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

        @Query("SELECT COUNT(DISTINCT o.buyerId) FROM Order o WHERE o.sellerId = :sellerId AND o.createdAt BETWEEN :startDate AND :endDate AND o.buyerId NOT IN (SELECT DISTINCT o2.buyerId FROM Order o2 WHERE o2.sellerId = :sellerId AND o2.createdAt < :startDate)")
        Integer countNewCustomersBySeller(@Param("sellerId") UUID sellerId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

        // Revenue chart data queries
        @Query("SELECT DATE_FORMAT(o.createdAt, '%Y-%m-%d') as period, COALESCE(SUM(o.totalAmount), 0) as revenue, COUNT(o) as orders FROM Order o WHERE o.sellerId = :sellerId AND o.status IN ('COMPLETED', 'DELIVERED') AND o.createdAt BETWEEN :startDate AND :endDate GROUP BY DATE_FORMAT(o.createdAt, '%Y-%m-%d') ORDER BY period")
        List<Object[]> getRevenueChartDataByDay(@Param("sellerId") UUID sellerId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

        @Query("SELECT DATE_FORMAT(o.createdAt, '%Y-%u') as period, COALESCE(SUM(o.totalAmount), 0) as revenue, COUNT(o) as orders FROM Order o WHERE o.sellerId = :sellerId AND o.status IN ('COMPLETED', 'DELIVERED') AND o.createdAt BETWEEN :startDate AND :endDate GROUP BY DATE_FORMAT(o.createdAt, '%Y-%u') ORDER BY period")
        List<Object[]> getRevenueChartDataByWeek(@Param("sellerId") UUID sellerId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

        @Query("SELECT DATE_FORMAT(o.createdAt, '%Y-%m') as period, COALESCE(SUM(o.totalAmount), 0) as revenue, COUNT(o) as orders FROM Order o WHERE o.sellerId = :sellerId AND o.status IN ('COMPLETED', 'DELIVERED') AND o.createdAt BETWEEN :startDate AND :endDate GROUP BY DATE_FORMAT(o.createdAt, '%Y-%m') ORDER BY period")
        List<Object[]> getRevenueChartDataByMonth(@Param("sellerId") UUID sellerId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

        // Top products queries
        @Query("SELECT oi.productId, SUM(oi.quantity) as totalSales, SUM(oi.subtotal) as totalRevenue FROM OrderItem oi JOIN oi.order o WHERE o.sellerId = :sellerId AND o.status IN ('COMPLETED', 'DELIVERED') AND o.createdAt BETWEEN :startDate AND :endDate GROUP BY oi.productId ORDER BY totalSales DESC")
        List<Object[]> getTopProductsBySales(@Param("sellerId") UUID sellerId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);

        @Query("SELECT oi.productId, SUM(oi.quantity) as totalSales, SUM(oi.subtotal) as totalRevenue FROM OrderItem oi JOIN oi.order o WHERE o.sellerId = :sellerId AND o.status IN ('COMPLETED', 'DELIVERED') AND o.createdAt BETWEEN :startDate AND :endDate GROUP BY oi.productId ORDER BY totalRevenue DESC")
        List<Object[]> getTopProductsByRevenue(@Param("sellerId") UUID sellerId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);

        // Customer analytics queries
        @Query("SELECT o.buyerId, COUNT(o) as totalOrders, SUM(o.totalAmount) as totalSpent FROM Order o WHERE o.sellerId = :sellerId AND o.status IN ('COMPLETED', 'DELIVERED') AND o.createdAt BETWEEN :startDate AND :endDate GROUP BY o.buyerId ORDER BY totalSpent DESC")
        List<Object[]> getCustomerAnalytics(@Param("sellerId") UUID sellerId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);

        @Query("SELECT AVG(CAST(orderCount AS DOUBLE)) FROM (SELECT COUNT(o) as orderCount FROM Order o WHERE o.sellerId = :sellerId AND o.status IN ('COMPLETED', 'DELIVERED') AND o.createdAt BETWEEN :startDate AND :endDate GROUP BY o.buyerId) AS subquery")
        Double getAverageOrdersPerCustomer(@Param("sellerId") UUID sellerId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

}
