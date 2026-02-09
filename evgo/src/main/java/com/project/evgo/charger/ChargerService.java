package com.project.evgo.charger;

import com.project.evgo.charger.internal.ChargerStatisticProjection;
import com.project.evgo.charger.response.ChargerResponse;
import com.project.evgo.charger.response.PortResponse;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for charger and port management.
 * Public API - accessible by other modules.
 */
public interface ChargerService {

    List<ChargerResponse> findByStationId(Long stationId);

    Optional<ChargerResponse> findById(Long id);

    List<PortResponse> findPortsByChargerId(Long chargerId);

    Optional<PortResponse> findPortById(Long id);

    List<ChargerStatisticProjection> findStatisticsByStationId(Long stationId);
}
