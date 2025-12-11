package com.project.evgo.review.internal;

import com.project.evgo.review.response.ReviewResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Converter for Review entity to DTO.
 */
@Component
public class ReviewDtoConverter {

    public ReviewResponse toResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .userId(review.getUserId())
                .stationId(review.getStationId())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }

    public List<ReviewResponse> toResponseList(List<Review> reviews) {
        return reviews.stream()
                .map(this::toResponse)
                .toList();
    }

    public Optional<ReviewResponse> toResponse(Optional<Review> review) {
        return review.map(this::toResponse);
    }
}
