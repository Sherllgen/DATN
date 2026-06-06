package com.project.evgo.review.internal;

import com.project.evgo.review.response.ReviewResponse;
import com.project.evgo.review.response.StationReviewsSummaryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReviewDtoConverterTest {

    private ReviewDtoConverter converter;

    @BeforeEach
    void setUp() {
        converter = new ReviewDtoConverter();
    }

    @Test
    void toResponse_fromReview_shouldMapFieldsCorrectly() {
        // Arrange
        Review review = new Review();
        review.setId(1L);
        review.setUserId(100L);
        review.setStationId(200L);
        review.setRating(5);
        review.setComment("Great station! <script>alert('xss');</script>");
        review.setCreatedAt(LocalDateTime.of(2023, Month.JANUARY, 1, 10, 0, 0));
        review.setUpdatedAt(LocalDateTime.of(2023, Month.JANUARY, 1, 12, 0, 0));

        Long currentUserId = 100L;

        // Act
        ReviewResponse response = converter.toResponse(review, currentUserId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getRating()).isEqualTo(5);
        assertThat(response.getComment()).isEqualTo("Great station!");
        assertThat(response.getCreatedAt()).isEqualTo("2023-01-01T10:00:00Z");
        assertThat(response.getUpdatedAt()).isEqualTo("2023-01-01T12:00:00Z");
        assertThat(response.getIsOwner()).isTrue();
    }

    @Test
    void toResponse_fromReview_withNullDates_shouldMapCorrectly() {
        // Arrange
        Review review = new Review();
        review.setId(1L);
        review.setUserId(100L);
        review.setStationId(200L);
        review.setRating(5);
        review.setComment(null);
        review.setCreatedAt(null);
        review.setUpdatedAt(null);

        Long currentUserId = 101L;

        // Act
        ReviewResponse response = converter.toResponse(review, currentUserId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getRating()).isEqualTo(5);
        assertThat(response.getComment()).isNull();
        assertThat(response.getCreatedAt()).isNull();
        assertThat(response.getUpdatedAt()).isNull();
        assertThat(response.getIsOwner()).isFalse();
    }

    @Test
    void toResponse_fromProjection_shouldMapFieldsCorrectly() {
        // Arrange
        ReviewProjection projection = mock(ReviewProjection.class);
        when(projection.getId()).thenReturn(2L);
        when(projection.getUserId()).thenReturn(200L);
        when(projection.getUserName()).thenReturn("John Doe");
        when(projection.getUserAvatar()).thenReturn("avatar.png");
        when(projection.getRating()).thenReturn(4);
        when(projection.getComment()).thenReturn("Good <b>service</b>");
        when(projection.getCreatedAt()).thenReturn(LocalDateTime.of(2023, Month.FEBRUARY, 1, 10, 0, 0));
        when(projection.getUpdatedAt()).thenReturn(LocalDateTime.of(2023, Month.FEBRUARY, 1, 12, 0, 0));

        Long currentUserId = 201L;

        // Act
        ReviewResponse response = converter.toResponse(projection, currentUserId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getUserName()).isEqualTo("John Doe");
        assertThat(response.getUserAvatar()).isEqualTo("avatar.png");
        assertThat(response.getRating()).isEqualTo(4);
        assertThat(response.getComment()).isEqualTo("Good service"); // HTML stripped
        assertThat(response.getCreatedAt()).isEqualTo("2023-02-01T10:00:00Z");
        assertThat(response.getUpdatedAt()).isEqualTo("2023-02-01T12:00:00Z");
        assertThat(response.getIsOwner()).isFalse();
    }

    @Test
    void toResponse_fromProjection_withNulls_shouldMapCorrectly() {
        // Arrange
        ReviewProjection projection = mock(ReviewProjection.class);
        when(projection.getId()).thenReturn(2L);
        when(projection.getUserId()).thenReturn(200L);
        when(projection.getUserName()).thenReturn(null);
        when(projection.getUserAvatar()).thenReturn(null);
        when(projection.getRating()).thenReturn(4);
        when(projection.getComment()).thenReturn(null);
        when(projection.getCreatedAt()).thenReturn(null);
        when(projection.getUpdatedAt()).thenReturn(null);

        Long currentUserId = 200L;

        // Act
        ReviewResponse response = converter.toResponse(projection, currentUserId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getUserName()).isNull();
        assertThat(response.getUserAvatar()).isNull();
        assertThat(response.getRating()).isEqualTo(4);
        assertThat(response.getComment()).isNull();
        assertThat(response.getCreatedAt()).isNull();
        assertThat(response.getUpdatedAt()).isNull();
        assertThat(response.getIsOwner()).isTrue();
    }

    @Test
    void toResponseList_shouldMapAllElements() {
        // Arrange
        Review review1 = new Review();
        review1.setId(1L);
        review1.setUserId(100L);
        review1.setRating(5);

        Review review2 = new Review();
        review2.setId(2L);
        review2.setUserId(101L);
        review2.setRating(4);

        // Act
        List<ReviewResponse> responses = converter.toResponseList(List.of(review1, review2), 100L);

        // Assert
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getId()).isEqualTo(1L);
        assertThat(responses.get(0).getIsOwner()).isTrue();
        assertThat(responses.get(1).getId()).isEqualTo(2L);
        assertThat(responses.get(1).getIsOwner()).isFalse();
    }

    @Test
    void toResponseOptional_shouldMapIfPresent() {
        // Arrange
        Review review = new Review();
        review.setId(1L);
        review.setUserId(100L);
        review.setRating(5);

        // Act
        Optional<ReviewResponse> response = converter.toResponse(Optional.of(review), 100L);

        // Assert
        assertThat(response).isPresent();
        assertThat(response.get().getId()).isEqualTo(1L);
        assertThat(response.get().getIsOwner()).isTrue();
    }

    @Test
    void toResponseOptional_shouldReturnEmptyIfEmpty() {
        // Act
        Optional<ReviewResponse> response = converter.toResponse(Optional.empty(), 100L);

        // Assert
        assertThat(response).isEmpty();
    }

    @Test
    void toSummaryResponse_shouldMapDistributionCorrectly() {
        // Arrange
        Double averageRating = 4.45;
        Long totalReviews = 100L;
        List<Object[]> distribution = List.of(
                new Object[]{5, 50L},
                new Object[]{4, 40L},
                new Object[]{3, 10L}
        );

        // Act
        StationReviewsSummaryResponse summary = converter.toSummaryResponse(averageRating, totalReviews, distribution);

        // Assert
        assertThat(summary).isNotNull();
        assertThat(summary.averageRating()).isEqualTo(4.5); // Rounding check
        assertThat(summary.totalReviews()).isEqualTo(100L);
        Map<Integer, Long> ratingDistribution = summary.ratingDistribution();
        assertThat(ratingDistribution).hasSize(3);
        assertThat(ratingDistribution.get(5)).isEqualTo(50L);
        assertThat(ratingDistribution.get(4)).isEqualTo(40L);
        assertThat(ratingDistribution.get(3)).isEqualTo(10L);
    }

    @Test
    void toSummaryResponse_withNulls_shouldHandleCorrectly() {
        // Arrange
        Double averageRating = null;
        Long totalReviews = null;
        List<Object[]> distribution = List.of();

        // Act
        StationReviewsSummaryResponse summary = converter.toSummaryResponse(averageRating, totalReviews, distribution);

        // Assert
        assertThat(summary).isNotNull();
        assertThat(summary.averageRating()).isEqualTo(0.0);
        assertThat(summary.totalReviews()).isEqualTo(0L);
        assertThat(summary.ratingDistribution()).isEmpty();
    }
}
