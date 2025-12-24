package com.project.evgo.review.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository for Review entity.
 */
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByStationId(Long stationId);

    List<Review> findByUserId(Long userId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.stationId = :stationId")
    Double getAverageRatingByStationId(@Param("stationId") Long stationId);
}
