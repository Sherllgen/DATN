package com.project.evgo.review;

import com.project.evgo.review.internal.Review;
import com.project.evgo.review.internal.ReviewDtoConverter;
import com.project.evgo.review.internal.ReviewProjection;
import com.project.evgo.review.internal.ReviewRepository;
import com.project.evgo.review.internal.ReviewServiceImpl;
import com.project.evgo.review.request.CreateReviewRequest;
import com.project.evgo.review.request.UpdateReviewRequest;
import com.project.evgo.review.response.ReviewResponse;
import com.project.evgo.review.response.StationReviewsSummaryResponse;
import com.project.evgo.sharedkernel.dto.PageResponse;
import com.project.evgo.sharedkernel.enums.ErrorCode;
import com.project.evgo.sharedkernel.exceptions.AppException;
import com.project.evgo.user.security.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ReviewService — TDD approach (RED phase first).
 */
@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @InjectMocks
    private ReviewServiceImpl reviewService;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewDtoConverter converter;

    private static final Long STATION_ID = 1L;
    private static final Long USER_ID = 10L;

    private Review testReview;
    private ReviewResponse testReviewResponse;

    @BeforeEach
    void setUp() {
        testReview = new Review();
        testReview.setId(1L);
        testReview.setStationId(STATION_ID);
        testReview.setUserId(USER_ID);
        testReview.setRating(5);
        testReview.setComment("Great station!");
        testReview.setCreatedAt(LocalDateTime.now());

        testReviewResponse = ReviewResponse.builder()
                .id(1L)
                .userName("Test User")
                .userAvatar("https://example.com/avatar.jpg")
                .rating(5)
                .comment("Great station!")
                .createdAt("2026-03-20T19:40:00Z")
                .build();
    }

    // ==================== GET REVIEW SUMMARY ====================

    @Nested
    @DisplayName("Get Review Summary Tests")
    class GetReviewSummaryTests {

        @Test
        @DisplayName("Should return full summary for a station with reviews")
        void getReviewSummary_ValidStation_ReturnsSummary() {
            // Given
            List<Object[]> distribution = List.of(
                    new Object[] { 5, 80L },
                    new Object[] { 4, 30L },
                    new Object[] { 3, 10L },
                    new Object[] { 2, 5L },
                    new Object[] { 1, 3L });
            StationReviewsSummaryResponse expectedSummary = new StationReviewsSummaryResponse(
                    4.5, 128L, Map.of(5, 80L, 4, 30L, 3, 10L, 2, 5L, 1, 3L));

            when(reviewRepository.getAverageRatingByStationId(STATION_ID)).thenReturn(4.5);
            when(reviewRepository.countByStationId(STATION_ID)).thenReturn(128L);
            when(reviewRepository.getRatingDistributionByStationId(STATION_ID)).thenReturn(distribution);
            when(converter.toSummaryResponse(4.5, 128L, distribution)).thenReturn(expectedSummary);

            // When
            StationReviewsSummaryResponse result = reviewService.getReviewSummary(STATION_ID);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.averageRating()).isEqualTo(4.5);
            assertThat(result.totalReviews()).isEqualTo(128L);
            assertThat(result.ratingDistribution()).containsKey(5);
            verify(reviewRepository).getAverageRatingByStationId(STATION_ID);
            verify(reviewRepository).countByStationId(STATION_ID);
            verify(reviewRepository).getRatingDistributionByStationId(STATION_ID);
        }

        @Test
        @DisplayName("Should return zero values when station has no reviews")
        void getReviewSummary_NoReviews_ReturnsZeroValues() {
            // Given
            List<Object[]> emptyDistribution = List.of();
            StationReviewsSummaryResponse emptySummary = new StationReviewsSummaryResponse(
                    0.0, 0L, Map.of());

            when(reviewRepository.getAverageRatingByStationId(STATION_ID)).thenReturn(null);
            when(reviewRepository.countByStationId(STATION_ID)).thenReturn(0L);
            when(reviewRepository.getRatingDistributionByStationId(STATION_ID)).thenReturn(emptyDistribution);
            when(converter.toSummaryResponse(null, 0L, emptyDistribution)).thenReturn(emptySummary);

            // When
            StationReviewsSummaryResponse result = reviewService.getReviewSummary(STATION_ID);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.totalReviews()).isEqualTo(0L);
            assertThat(result.ratingDistribution()).isEmpty();
        }
    }

    // ==================== GET STATION REVIEWS (PAGINATED) ====================

    @Nested
    @DisplayName("Get Station Reviews (Paginated) Tests")
    class GetStationReviewsTests {

        @Test
        @DisplayName("Should return paginated reviews with user info")
        void getStationReviews_ValidStation_ReturnsPage() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            ReviewProjection mockProjection = mock(ReviewProjection.class);
            Page<ReviewProjection> projectionPage = new PageImpl<>(List.of(mockProjection), pageable, 1);

            when(reviewRepository.findByStationIdWithUser(STATION_ID, pageable)).thenReturn(projectionPage);
            when(converter.toResponse(mockProjection)).thenReturn(testReviewResponse);

            // When
            PageResponse<ReviewResponse> result = reviewService.getStationReviews(STATION_ID, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.content()).hasSize(1);
            assertThat(result.content().get(0).getUserName()).isEqualTo("Test User");
            assertThat(result.totalElements()).isEqualTo(1L);
            verify(reviewRepository).findByStationIdWithUser(STATION_ID, pageable);
        }

        @Test
        @DisplayName("Should return empty page when no reviews exist")
        void getStationReviews_EmptyResult_ReturnsEmptyPage() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<ReviewProjection> emptyPage = new PageImpl<>(List.of(), pageable, 0);

            when(reviewRepository.findByStationIdWithUser(STATION_ID, pageable)).thenReturn(emptyPage);

            // When
            PageResponse<ReviewResponse> result = reviewService.getStationReviews(STATION_ID, pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.content()).isEmpty();
            assertThat(result.totalElements()).isEqualTo(0L);
            verify(converter, never()).toResponse(any(ReviewProjection.class));
        }
    }

    // ==================== CREATE REVIEW ====================

    @Nested
    @DisplayName("Create Review Tests")
    class CreateReviewTests {

        @Test
        @DisplayName("Should save review and return response when request is valid")
        void createReview_ValidRequest_SavesAndReturns() {
            // Given
            CreateReviewRequest request = new CreateReviewRequest(5, "Great station!");

            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(USER_ID);
                when(reviewRepository.save(any(Review.class))).thenReturn(testReview);
                when(converter.toResponse(testReview)).thenReturn(testReviewResponse);

                // When
                ReviewResponse result = reviewService.createReview(STATION_ID, request);

                // Then
                assertThat(result).isNotNull();
                assertThat(result.getRating()).isEqualTo(5);
                verify(reviewRepository).save(argThat(r -> r.getStationId().equals(STATION_ID) &&
                        r.getUserId().equals(USER_ID) &&
                        r.getRating() == 5));
            }
        }

        @Test
        @DisplayName("Should throw NullPointerException when request is null")
        void createReview_NullRequest_ThrowsException() {
            // When / Then
            assertThatThrownBy(() -> reviewService.createReview(STATION_ID, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // ==================== UPDATE REVIEW ====================

    @Nested
    @DisplayName("Update Review Tests")
    class UpdateReviewTests {

        @Test
        @DisplayName("Should update review when owner requests")
        void updateReview_ValidOwner_UpdatesAndReturns() {
            // Given
            UpdateReviewRequest request = new UpdateReviewRequest(4, "Updated comment");
            
            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(USER_ID);
                when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));
                when(reviewRepository.save(any(Review.class))).thenReturn(testReview);
                when(converter.toResponse(any(Review.class))).thenReturn(testReviewResponse);

                // When
                ReviewResponse result = reviewService.updateReview(1L, request);

                // Then
                assertThat(result).isNotNull();
                verify(reviewRepository).save(argThat(r -> r.getRating().equals(4) && 
                                                          r.getComment().equals("Updated comment")));
            }
        }

        @Test
        @DisplayName("Should throw AppException when user is not the owner")
        void updateReview_NotOwner_ThrowsForbidden() {
            // Given
            UpdateReviewRequest request = new UpdateReviewRequest(4, "Updated comment");
            Long anotherUserId = 99L;

            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(anotherUserId);
                when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));

                // When / Then
                assertThatThrownBy(() -> reviewService.updateReview(1L, request))
                        .isInstanceOf(AppException.class)
                        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REVIEW_NOT_OWNED);
            }
        }
    }

    // ==================== DELETE REVIEW ====================

    @Nested
    @DisplayName("Delete Review Tests")
    class DeleteReviewTests {

        @Test
        @DisplayName("Should delete review when owner requests")
        void deleteReview_ValidOwner_DeletesReview() {
            // Given
            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(USER_ID);
                when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));

                // When
                reviewService.deleteReview(1L);

                // Then
                verify(reviewRepository).delete(testReview);
            }
        }

        @Test
        @DisplayName("Should throw AppException when user is not the owner")
        void deleteReview_NotOwner_ThrowsForbidden() {
            // Given
            Long anotherUserId = 99L;

            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(anotherUserId);
                when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));

                // When / Then
                assertThatThrownBy(() -> reviewService.deleteReview(1L))
                        .isInstanceOf(AppException.class)
                        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REVIEW_NOT_OWNED);
            }
        }
    }
}

