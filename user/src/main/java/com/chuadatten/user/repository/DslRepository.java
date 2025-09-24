package com.chuadatten.user.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.chuadatten.user.dto.SellerRatingDto;
import com.chuadatten.user.entity.QSellerRating;
import com.chuadatten.user.entity.SellerRating;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class DslRepository {
    private final JPAQueryFactory jpaQueryFactory;

    public Page<SellerRating> getSellerRatingDynamic(UUID userId, SellerRatingDto request, Pageable pageable) {
        QSellerRating qSellerRating = QSellerRating.sellerRating;
        BooleanBuilder where = new BooleanBuilder();
        where.and(qSellerRating.seller.id.eq(userId));
        if (request.getCreatedAt() != null) {
            where.and(qSellerRating.createdAt.eq(request.getCreatedAt()));

        }
        if (request.getRating() != null) {
            where.and(qSellerRating.ratingScore.eq(request.getRating()));

        }

        List<SellerRating> content = jpaQueryFactory
                .selectFrom(qSellerRating)
                .where(where)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = jpaQueryFactory
                .select(qSellerRating.count())
                .from(qSellerRating)
                .where(where)
                .fetchOne();

        return new PageImpl<>(content, pageable, total);

    }

}
