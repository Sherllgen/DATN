package com.project.evgo.station;

import com.project.evgo.review.ReviewService;
import com.project.evgo.sharedkernel.events.ReviewPostedEvent;
import com.project.evgo.station.internal.Station;
import com.project.evgo.station.internal.StationRatingUpdatedListener;
import com.project.evgo.station.internal.StationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for StationRatingUpdatedListener.
 * Verifies that Station.rate is kept in sync whenever a ReviewPostedEvent fires.
 */
@ExtendWith(MockitoExtension.class)
class StationRatingUpdatedListenerTest {

    @InjectMocks
    private StationRatingUpdatedListener listener;

    @Mock
    private StationRepository stationRepository;

    @Mock
    private ReviewService reviewService;

    private static final Long STATION_ID = 42L;

    private Station testStation;

    @BeforeEach
    void setUp() {
        testStation = new Station();
        testStation.setId(STATION_ID);
        testStation.setName("Test Station");
        testStation.setRate(null);
    }

    @Nested
    @DisplayName("onReviewPosted — happy path")
    class HappyPath {

        @Test
        @DisplayName("Should update Station.rate with new average when station exists")
        void onReviewPosted_StationExists_UpdatesRate() {
            // Given
            Double newAverage = 4.3;
            ReviewPostedEvent event = new ReviewPostedEvent(STATION_ID);

            when(reviewService.getAverageRatingByStationId(STATION_ID)).thenReturn(newAverage);
            when(stationRepository.findById(STATION_ID)).thenReturn(Optional.of(testStation));
            when(stationRepository.save(any(Station.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            listener.onReviewPosted(event);

            // Then
            verify(stationRepository).save(argThat(s -> {
                assertThat(s.getRate()).isEqualTo(newAverage);
                return true;
            }));
        }

        @Test
        @DisplayName("Should set Station.rate to null when there are no more reviews")
        void onReviewPosted_NoReviewsLeft_SetsRateToNull() {
            // Given — calculateAverage returns null when no reviews exist
            ReviewPostedEvent event = new ReviewPostedEvent(STATION_ID);
            testStation.setRate(4.5); // previously had a rating

            when(reviewService.getAverageRatingByStationId(STATION_ID)).thenReturn(null);
            when(stationRepository.findById(STATION_ID)).thenReturn(Optional.of(testStation));
            when(stationRepository.save(any(Station.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            listener.onReviewPosted(event);

            // Then
            verify(stationRepository).save(argThat(s -> s.getRate() == null));
        }
    }

    @Nested
    @DisplayName("onReviewPosted — edge cases")
    class EdgeCases {

        @Test
        @DisplayName("Should skip save and log warning when stationId does not exist")
        void onReviewPosted_StationNotFound_DoesNotSave() {
            // Given
            ReviewPostedEvent event = new ReviewPostedEvent(STATION_ID);

            when(reviewService.getAverageRatingByStationId(STATION_ID)).thenReturn(4.0);
            when(stationRepository.findById(STATION_ID)).thenReturn(Optional.empty());

            // When — must NOT throw
            listener.onReviewPosted(event);

            // Then
            verify(stationRepository, never()).save(any());
        }
    }
}
