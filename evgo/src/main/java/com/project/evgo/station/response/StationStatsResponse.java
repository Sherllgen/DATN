package com.project.evgo.station.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for Station module statistics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StationStatsResponse {
    private long totalStations;
    private long activeStations;
}
