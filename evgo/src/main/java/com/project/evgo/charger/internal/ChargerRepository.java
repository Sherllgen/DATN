package com.project.evgo.charger.internal;

import com.project.evgo.sharedkernel.enums.ChargerStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for Charger entity.
 */
public interface ChargerRepository extends JpaRepository<Charger, Long> {

    List<Charger> findByStationId(Long stationId);

    long countByStationId(Long stationId);

    long countByStationIdAndStatus(Long stationId, ChargerStatus status);
}
