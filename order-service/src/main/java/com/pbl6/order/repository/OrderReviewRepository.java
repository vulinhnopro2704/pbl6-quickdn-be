package com.pbl6.order.repository;

import com.pbl6.order.entity.OrderReviewEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderReviewRepository extends JpaRepository<OrderReviewEntity, UUID> {
    Optional<OrderReviewEntity> findByOrderId(UUID orderId);
    // AVG
    @Query("SELECT AVG(r.rating) FROM OrderReviewEntity r WHERE r.shipperId = :shipperId")
    Double findAverageRatingByShipperId(UUID shipperId);

    // COUNT
    @Query("SELECT COUNT(r) FROM OrderReviewEntity r WHERE r.shipperId = :shipperId")
    Long countByShipperId(UUID shipperId);

    // Distribution: rating -> count
    @Query("SELECT r.rating, COUNT(r) FROM OrderReviewEntity r WHERE r.shipperId = :shipperId GROUP BY r.rating")
    List<Object[]> findRatingDistributionByShipperId(UUID shipperId);

    // Recent reviews (paging)
    Page<OrderReviewEntity> findByShipperIdOrderByCreatedAtDesc(UUID shipperId, Pageable pageable);
}
