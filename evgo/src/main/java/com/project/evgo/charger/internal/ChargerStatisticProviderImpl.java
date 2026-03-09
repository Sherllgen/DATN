package com.project.evgo.charger.internal;

import com.project.evgo.charger.ChargerService;
import com.project.evgo.station.ChargerStatisticProvider;
import com.project.evgo.station.StationChargerStatistic;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

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
    public List<StationChargerStatistic> getStatistics(Long stationId) {
        return chargerService.findStatisticsByStationId(stationId).stream()
                .map(stat -> new StationChargerStatistic(stat.type(), stat.status(), stat.count()))
                .toList();
    }
}
