package com.project.evgo.station.internal;

import com.project.evgo.review.ReviewService;
import com.project.evgo.sharedkernel.events.ReviewPostedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Listens for {@link ReviewPostedEvent} and keeps {@link Station#rate}
 * in sync with the current average rating for that station.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StationRatingUpdatedListener {

    private final StationRepository stationRepository;
    private final ReviewService reviewService;

    /**
     * Re-calculates the average rating for the station associated with the
     * posted review and persists the result to {@code stations.rate}.
     */
    @ApplicationModuleListener
    @Transactional
    public void onReviewPosted(ReviewPostedEvent event) {
        Long stationId = event.stationId();
        Double average = reviewService.getAverageRatingByStationId(stationId);

        stationRepository.findById(stationId).ifPresentOrElse(
                station -> {
                    station.setRate(average);
                    stationRepository.save(station);
                    log.debug("Station {} rate updated to {}", stationId, average);
                },
                () -> log.warn("ReviewPostedEvent received for unknown stationId={}", stationId)
        );
    }
}
