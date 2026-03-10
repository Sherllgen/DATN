package com.project.evgo.station;

import java.util.List;

/**
 * Interface to provide port counts for stations.
 * Implemented by charger module to avoid circular dependency.
 */
public interface PortCountProvider {
    PortCounts getPortCounts(Long stationId);

    List<StationPortSummary> getPortSummaries(Long stationId);
}
