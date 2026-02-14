package com.project.evgo.station.response;

import com.project.evgo.sharedkernel.enums.StationStatus;
import lombok.Builder;

@Builder
public record StationSearchResult(
                Long id,
                // Long ownerId,
                String name,
                // String description,
                String address,
                Double latitude,
                Double longitude,
                Double rate,
                StationStatus status,
                // List<String> imageUrls,
                Boolean isFlaggedLowQuality,
                Double distanceKm,
                Integer availableChargersCount,
                Integer totalChargersCount
// LocalDateTime createdAt,
// LocalDateTime updatedAt
) {
}
