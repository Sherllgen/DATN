package com.project.evgo.station;

import java.util.List;

/**
 * Interface for providing charger statistics to the station module.
 * Public API. To be implemented by the charger module.
 */
public interface ChargerStatisticProvider {
    List<StationChargerStatistic> getStatistics(Long stationId);
}
