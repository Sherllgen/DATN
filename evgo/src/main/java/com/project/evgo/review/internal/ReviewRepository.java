package com.project.evgo.review.internal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Review entity.
 */
public interface ReviewRepository extends JpaRepository<Review, Long> {

        List<Review> findByStationId(Long stationId);

        List<Review> findByUserId(Long userId);

        Optional<Review> findByUserIdAndStationId(Long userId, Long stationId);

        long countByStationId(Long stationId);

        @Query("SELECT AVG(r.rating) FROM Review r WHERE r.stationId = :stationId")
        Double getAverageRatingByStationId(@Param("stationId") Long stationId);

        @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.stationId = :stationId GROUP BY r.rating")
        List<Object[]> getRatingDistributionByStationId(@Param("stationId") Long stationId);

        @Query(value = "SELECT r.id, r.user_id AS userId, u.full_name AS userName, u.avatar_url AS userAvatar, " +
                        "r.rating, r.comment, r.created_at AS createdAt, r.updated_at AS updatedAt " +
                        "FROM reviews r JOIN users u ON r.user_id = u.id " +
                        "WHERE r.station_id = :stationId", countQuery = "SELECT COUNT(*) FROM reviews WHERE station_id = :stationId", nativeQuery = true)

        Page<ReviewProjection> findByStationIdWithUser(
                        @Param("stationId") Long stationId, Pageable pageable);
}
