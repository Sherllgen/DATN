package com.project.evgo.review.internal;

import com.project.evgo.review.ReviewService;
import com.project.evgo.review.request.CreateReviewRequest;
import com.project.evgo.review.request.UpdateReviewRequest;
import com.project.evgo.review.response.ReviewResponse;
import com.project.evgo.review.response.StationReviewsSummaryResponse;
import com.project.evgo.sharedkernel.dto.PageResponse;
import com.project.evgo.sharedkernel.enums.ErrorCode;
import com.project.evgo.sharedkernel.exceptions.AppException;
import com.project.evgo.user.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of ReviewService.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewDtoConverter converter;

    @Override
    public Optional<ReviewResponse> findById(Long id) {
        return converter.toResponse(reviewRepository.findById(id));
    }

    @Override
    public List<ReviewResponse> findByStationId(Long stationId) {
        return converter.toResponseList(reviewRepository.findByStationId(stationId));
    }

    @Override
    public List<ReviewResponse> findByUserId(Long userId) {
        return converter.toResponseList(reviewRepository.findByUserId(userId));
    }

    @Override
    public Double getAverageRatingByStationId(Long stationId) {
        return reviewRepository.getAverageRatingByStationId(stationId);
    }

    @Override
    public StationReviewsSummaryResponse getReviewSummary(Long stationId) {
        Double averageRating = reviewRepository.getAverageRatingByStationId(stationId);
        Long totalReviews = reviewRepository.countByStationId(stationId);
        List<Object[]> distribution = reviewRepository.getRatingDistributionByStationId(stationId);
        return converter.toSummaryResponse(averageRating, totalReviews, distribution);
    }

    @Override
    public PageResponse<ReviewResponse> getStationReviews(Long stationId, Pageable pageable) {
        Page<ReviewProjection> projectionPage =
                reviewRepository.findByStationIdWithUser(stationId, pageable);
        Page<ReviewResponse> responsePage = projectionPage.map(converter::toResponse);
        return PageResponse.of(responsePage);
    }

    @Override
    @Transactional
    public ReviewResponse createReview(Long stationId, CreateReviewRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        Review review = new Review();
        review.setStationId(stationId);
        review.setUserId(userId);
        review.setRating(request.rating());
        review.setComment(request.comment());
        review.setUpdatedAt(java.time.LocalDateTime.now());
        Review saved = reviewRepository.save(review);
        return converter.toResponse(saved);
    }

    @Override
    @Transactional
    public ReviewResponse updateReview(Long id, UpdateReviewRequest request) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));

        validateOwnership(review);

        review.setRating(request.rating());
        review.setComment(request.comment());
        review.setUpdatedAt(java.time.LocalDateTime.now());

        Review saved = reviewRepository.save(review);
        return converter.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteReview(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));

        validateOwnership(review);

        reviewRepository.delete(review);
    }

    private void validateOwnership(Review review) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (!review.getUserId().equals(currentUserId)) {
            throw new AppException(ErrorCode.REVIEW_NOT_OWNED);
        }
    }
}


