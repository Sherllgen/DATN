package com.project.evgo.charger.internal;

import com.project.evgo.charger.ChargerService;
import com.project.evgo.station.ChargerStatisticProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Implementation of ChargerStatisticProvider for the station module.
 * Located in charger module to prevent station module from depending on charger
 * module.
 */
@Component
@RequiredArgsConstructor
public class ChargerStatisticProviderImpl implements ChargerStatisticProvider {

    private final ChargerService chargerService;

    @Override
    public long getTotalChargerCount(Long stationId) {
        return chargerService.countByStationId(stationId);
    }

    @Override
    public long getAvailableChargerCount(Long stationId) {
        return chargerService.countAvailableByStationId(stationId);
    }
}
