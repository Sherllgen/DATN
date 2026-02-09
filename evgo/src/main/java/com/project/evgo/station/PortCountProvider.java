package com.project.evgo.station;

/**
 * Interface to provide port counts for stations.
 * Implemented by charger module to avoid circular dependency.
 */
public interface PortCountProvider {
    PortCounts getPortCounts(Long stationId);
}
