package com.project.evgo.station;

/**
 * Interface for providing charger statistics to the station module.
 * Public API. To be implemented by the charger module.
 */
public interface ChargerStatisticProvider {
    long getTotalChargerCount(Long stationId);

    long getAvailableChargerCount(Long stationId);
}
