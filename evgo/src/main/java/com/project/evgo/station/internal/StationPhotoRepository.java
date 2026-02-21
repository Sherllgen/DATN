package com.project.evgo.station.internal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for StationPhoto entity.
 * Internal - not accessible by other modules.
 */
public interface StationPhotoRepository extends JpaRepository<StationPhoto, Long> {

    List<StationPhoto> findByStationIdOrderByDisplayOrderAsc(Long stationId);

    int countByStationId(Long stationId);

    void deleteAllByStationId(Long stationId);
}
