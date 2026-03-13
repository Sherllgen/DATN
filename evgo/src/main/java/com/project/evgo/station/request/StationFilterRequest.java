package com.project.evgo.station.request;

import com.project.evgo.sharedkernel.enums.StationStatus;

import java.util.List;

/**
 * Request DTO for filtering stations.
 * All fields are optional — only provided fields are applied as filters.
 * Public — accessible by controller and other modules.
 */
public record StationFilterRequest(
        Double minPower,
        Double maxPower,
        List<String> connectorTypes,
        StationStatus status,
        Integer page,
        Integer size,
        String query,
        Double userLat,
        Double userLng) {
}
