package com.project.evgo.station.internal;

import com.project.evgo.station.response.StationResponse;
import com.project.evgo.station.response.StationSearchResult;
import com.project.evgo.sharedkernel.utils.GeoUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Converter for Station entity to StationResponse DTO.
 * Internal - handles entity to DTO mapping within the module.
 */
@Component
@RequiredArgsConstructor
public class StationDtoConverter {

    public StationResponse convert(Station from) {
        return StationResponse.builder()
                .id(from.getId())
                .ownerId(from.getOwnerId())
                .name(from.getName())
                .description(from.getDescription())
                .address(from.getAddress())
                .latitude(from.getLatitude())
                .longitude(from.getLongitude())
                .rate(from.getRate())
                .status(from.getStatus())
                .imageUrls(from.getImageUrls())
                .isFlaggedLowQuality(from.getIsFlaggedLowQuality())
                .createdAt(from.getCreatedAt())
                .updatedAt(from.getUpdatedAt())
                .build();
    }

    public List<StationResponse> convert(List<Station> from) {
        return from.stream().map(this::convert).toList();
    }

    public Optional<StationResponse> convert(Optional<Station> from) {
        return from.map(this::convert);
    }

    /**
     * Convert StationProjection results to StationSearchResult DTOs.
     * Charger counts are now fetched in the SQL query (no N+1 problem!)
     *
     * @param projections List of StationProjection from repository
     * @return List of StationSearchResult DTOs
     */
    public List<StationSearchResult> convertToSearchResults(List<StationProjection> projections) {
        if (projections == null || projections.isEmpty()) {
            return List.of();
        }

        return projections.stream()
                .map(projection -> {
                    // Convert distance from meters to km
                    Double distanceKm = GeoUtils.metersToKm(projection.getDistance());
                    if (distanceKm != null) {
                        distanceKm = GeoUtils.roundDistance(distanceKm);
                    }

                    // Charger counts now come from SQL subqueries (no extra queries!)
                    int totalChargers = projection.getTotalChargersCount() != null ? projection.getTotalChargersCount()
                            : 0;
                    int availableChargers = projection.getAvailableChargersCount() != null
                            ? projection.getAvailableChargersCount()
                            : 0;

                    // Build search result
                    return StationSearchResult.builder()
                            .id(projection.getId())
                            .ownerId(projection.getOwnerId())
                            .name(projection.getName())
                            .description(projection.getDescription())
                            .address(projection.getAddress())
                            .latitude(projection.getLatitude())
                            .longitude(projection.getLongitude())
                            .rate(projection.getRate())
                            .status(projection.getStatus())
                            .imageUrls(List.of()) // TODO: Add image URLs via JSON aggregation or separate query
                            .isFlaggedLowQuality(projection.getIsFlaggedLowQuality())
                            .distanceKm(distanceKm)
                            .availableChargersCount(availableChargers)
                            .totalChargersCount(totalChargers)
                            .createdAt(projection.getCreatedAt())
                            .updatedAt(projection.getUpdatedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }
}
