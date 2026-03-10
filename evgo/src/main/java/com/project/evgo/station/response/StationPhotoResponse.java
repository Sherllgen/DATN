package com.project.evgo.station.response;

import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Response DTO for station photo information.
 * Public API - accessible by other modules.
 */
@Builder
public record StationPhotoResponse(
        Long id,
        Long stationId,
        String imageUrl,
        String caption,
        Integer displayOrder,
        LocalDateTime createdAt) {
}
