package com.project.evgo.station.response;

import com.project.evgo.sharedkernel.enums.StationStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for station information.
 * Public API - accessible by other modules.
 */
@Builder
public record StationResponse(
    Long id,
    Long ownerId,
    String name,
    String description,
    String address,
    Double latitude,
    Double longitude,
    Double rate,
    StationStatus status,
    List<String> imageUrls,
    Boolean isFlaggedLowQuality,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {
}
