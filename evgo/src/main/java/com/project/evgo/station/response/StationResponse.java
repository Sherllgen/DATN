package com.project.evgo.station.response;

import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Response DTO for station information.
 * Public API - accessible by other modules.
 */
@Builder
public record StationResponse(
        Long id,
        String name,
        String address,
        Double latitude,
        Double longitude,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
