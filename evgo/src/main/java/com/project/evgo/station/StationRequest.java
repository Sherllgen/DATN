package com.project.evgo.station;

/**
 * Request DTO for station operations.
 * Public API - accessible by other modules.
 */
public record StationRequest(
        String name,
        String address,
        Double latitude,
        Double longitude) {
}
