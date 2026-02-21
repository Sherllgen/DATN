package com.project.evgo.station.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository for PriceSetting entity.
 * Internal - not accessible by other modules.
 */
public interface PriceSettingRepository extends JpaRepository<PriceSetting, Long> {

    /**
     * Find the active pricing for a station.
     */
    Optional<PriceSetting> findByStationIdAndIsActiveTrue(Long stationId);

    /**
     * Find all pricing versions for a station, ordered by version desc.
     */
    List<PriceSetting> findByStationIdOrderByVersionDesc(Long stationId);

    /**
     * Find the latest version for a station (for version incrementing).
     */
    Optional<PriceSetting> findTopByStationIdOrderByVersionDesc(Long stationId);

    /**
     * Deactivate all active pricing for a station.
     */
    @Modifying
    @Query("UPDATE PriceSetting p SET p.isActive = false WHERE p.stationId = :stationId AND p.isActive = true")
    void deactivateAllByStationId(@Param("stationId") Long stationId);
}
