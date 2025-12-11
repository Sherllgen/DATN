package com.project.evgo.review;

import com.project.evgo.review.response.ReviewResponse;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for review management.
 * Public API - accessible by other modules.
 */
public interface ReviewService {

    Optional<ReviewResponse> findById(Long id);

    List<ReviewResponse> findByStationId(Long stationId);

    List<ReviewResponse> findByUserId(Long userId);

    Double getAverageRatingByStationId(Long stationId);
}
