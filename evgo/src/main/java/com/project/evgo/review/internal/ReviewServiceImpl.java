package com.project.evgo.review.internal;

import com.project.evgo.review.ReviewService;
import com.project.evgo.review.response.ReviewResponse;
import lombok.RequiredArgsConstructor;
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
}
