package com.project.evgo.station.response;

import java.util.List;

/**
 * Response DTO for station filter metadata.
 * Provides data needed for the frontend to build filter UI.
 * Public — accessible by controller and other modules.
 */
public record StationMetadataResponse(
        Double minPower,
        Double maxPower,
        List<String> connectorTypes,
        List<String> statuses
) {
}
