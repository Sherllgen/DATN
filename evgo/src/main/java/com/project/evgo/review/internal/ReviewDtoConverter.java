package com.project.evgo.review.internal;

import com.project.evgo.review.response.ReviewResponse;
import com.project.evgo.review.response.StationReviewsSummaryResponse;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Converter for Review entity and projections to DTOs.
 * Internal - not accessible by other modules.
 */
@Component
public class ReviewDtoConverter {

    private static final DateTimeFormatter ISO_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    public ReviewResponse toResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .rating(review.getRating())
                .comment(sanitize(review.getComment()))
                .createdAt(review.getCreatedAt() != null
                        ? review.getCreatedAt().atOffset(ZoneOffset.UTC).format(ISO_FORMATTER) : null)
                .updatedAt(review.getUpdatedAt() != null
                        ? review.getUpdatedAt().atOffset(ZoneOffset.UTC).format(ISO_FORMATTER) : null)
                .build();
    }

    public ReviewResponse toResponse(ReviewProjection projection) {
        return ReviewResponse.builder()
                .id(projection.getId())
                .userName(projection.getUserName())
                .userAvatar(projection.getUserAvatar())
                .rating(projection.getRating())
                .comment(sanitize(projection.getComment()))
                .createdAt(projection.getCreatedAt() != null
                        ? projection.getCreatedAt().atOffset(ZoneOffset.UTC).format(ISO_FORMATTER) : null)
                .build(); // Note: Projection might need updatedAt if we want it there
    }

    public List<ReviewResponse> toResponseList(List<Review> reviews) {
        return reviews.stream()
                .map(this::toResponse)
                .toList();
    }

    public Optional<ReviewResponse> toResponse(Optional<Review> review) {
        return review.map(this::toResponse);
    }

    public StationReviewsSummaryResponse toSummaryResponse(
            Double averageRating, Long totalReviews, List<Object[]> distribution) {
        Map<Integer, Long> ratingDistribution = new HashMap<>();
        for (Object[] row : distribution) {
            Integer star = ((Number) row[0]).intValue();
            Long count = ((Number) row[1]).longValue();
            ratingDistribution.put(star, count);
        }
        return new StationReviewsSummaryResponse(
                averageRating != null ? Math.round(averageRating * 10.0) / 10.0 : 0.0,
                totalReviews != null ? totalReviews : 0L,
                ratingDistribution
        );
    }

    private String sanitize(String text) {
        if (text == null) {
            return null;
        }
        return Jsoup.clean(text, Safelist.none());
    }
}

