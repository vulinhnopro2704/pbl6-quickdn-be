package com.pbl6.order.dto;

import java.util.UUID;

public record ShipperRatingDto(
        UUID driverId,
        double averageRating,
        long totalReviews,
        java.util.Map<Integer, Long> ratingDistribution // keys 1..5
) {}
