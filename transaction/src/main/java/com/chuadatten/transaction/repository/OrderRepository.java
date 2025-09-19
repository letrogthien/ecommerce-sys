
package com.chuadatten.transaction.repository;

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


        //get order trạng thái order là ready_pay hoặc paying và payment status là pending và createdAt cách hiện tại hơn 30 phút
        @Query("SELECT o FROM Order o WHERE (o.status = 'READY_PAY' OR o.status = 'PAYING') AND o.paymentStatus = 'PENDING' AND o.createdAt <= CURRENT_TIMESTAMP - INTERVAL '30 minutes'")
        List<Order> findOrdersToCancel();

}
