
package com.chuadatten.transaction.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.chuadatten.transaction.common.Status;
import com.chuadatten.transaction.entity.OrderRefund;

@Repository
public interface OrderRefundRepository extends JpaRepository<OrderRefund, UUID> {

    Optional<OrderRefund> findByOrderIdAndRequestBy(UUID orderId, UUID requestBy);

    Page<OrderRefund> findByRequestBy(UUID buyerId, Pageable pageable);

    @Query("""
            SELECT o
            FROM OrderRefund o
            WHERE (:orderId IS NULL OR o.order.id = :orderId)
            AND (:buyerId IS NULL OR o.requestBy = :buyerId)

                """)
    Page<OrderRefund> findAllWithStatusAndOrderIdAndBuyerIdFilter(@Param("status") Status status,
            @Param("orderId") UUID orderId,
            @Param("buyerId") UUID buyerId,
            PageRequest of);

    // Query for seller refund management - JOIN with orders to get seller info
    @Query("""
            SELECT r FROM OrderRefund r 
            JOIN r.order o 
            WHERE o.sellerId = :sellerId 
            AND (:status IS NULL OR r.status = :status)
            ORDER BY r.createdAt DESC
            """)
    Page<OrderRefund> findRefundsBySeller(@Param("sellerId") UUID sellerId, 
                                         @Param("status") Status status, 
                                         Pageable pageable);

    // Query to find refund with seller validation
    @Query("""
            SELECT r FROM OrderRefund r 
            JOIN r.order o 
            WHERE r.id = :refundId 
            AND o.sellerId = :sellerId
            """)
    Optional<OrderRefund> findByIdAndSellerId(@Param("refundId") UUID refundId, 
                                             @Param("sellerId") UUID sellerId);

}
