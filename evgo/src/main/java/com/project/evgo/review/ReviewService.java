package com.project.evgo.review;

import com.project.evgo.review.request.CreateReviewRequest;
import com.project.evgo.review.response.ReviewResponse;
import com.project.evgo.review.response.StationReviewsSummaryResponse;
import com.project.evgo.sharedkernel.dto.PageResponse;
import org.springframework.data.domain.Pageable;

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

    StationReviewsSummaryResponse getReviewSummary(Long stationId);

    PageResponse<ReviewResponse> getStationReviews(Long stationId, Pageable pageable);

    ReviewResponse createReview(Long stationId, CreateReviewRequest request);

    ReviewResponse updateReview(Long id, com.project.evgo.review.request.UpdateReviewRequest request);

    void deleteReview(Long id);
}


