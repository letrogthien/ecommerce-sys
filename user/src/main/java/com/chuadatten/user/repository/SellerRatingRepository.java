package com.chuadatten.user.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.chuadatten.user.entity.SellerRating;

@Repository
public interface SellerRatingRepository extends JpaRepository<SellerRating, UUID>{
    
    @Query("SELECT sr FROM SellerRating sr WHERE sr.seller.id = :sellerId")
    Page<SellerRating> findBySellerId(@Param("sellerId") UUID sellerId, Pageable pageable);
    
    Page<SellerRating> findByRatingScore(Integer rating, Pageable pageable);
    
    @Query("SELECT sr FROM SellerRating sr WHERE sr.seller.id = :sellerId AND sr.ratingScore = :rating")
    Page<SellerRating> findBySellerIdAndRating(@Param("sellerId") UUID sellerId, @Param("rating") Integer rating, Pageable pageable);
}
