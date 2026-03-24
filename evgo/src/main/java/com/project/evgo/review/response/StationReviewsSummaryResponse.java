package com.project.evgo.review.response;

import java.util.Map;

/**
 * Response DTO for station review summary statistics.
 * Public API - accessible by other modules.
 */
public record StationReviewsSummaryResponse(
        Double averageRating,
        Long totalReviews,
        Map<Integer, Long> ratingDistribution
) {
}
