package com.project.evgo.station.internal;

import com.project.evgo.station.PortCountProvider;
import com.project.evgo.station.PortCounts;
import com.project.evgo.station.response.StationOpeningHoursResponse;
import com.project.evgo.charger.ChargerService;
import com.project.evgo.charger.internal.ChargerStatisticProjection;
import com.project.evgo.sharedkernel.enums.ChargerStatus;
import com.project.evgo.station.response.StationResponse;
import com.project.evgo.station.response.StationSearchResult;
import com.project.evgo.sharedkernel.utils.GeoUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Converter for Station entity to StationResponse DTO.
 * Internal - handles entity to DTO mapping within the module.
 */
@Component
@RequiredArgsConstructor
public class StationDtoConverter {

        private final PortCountProvider portCountProvider;
        private final ChargerService chargerService;

        public StationResponse convert(Station from) {
                // Calculate port counts using provider interface
                PortCounts counts = portCountProvider.getPortCounts(from.getId());
                int totalPorts = counts.totalPorts();
                int availablePorts = counts.availablePorts();

                // Get charger statistics directly from DB
                List<ChargerStatisticProjection> stats = chargerService.findStatisticsByStationId(from.getId());

                // Calculate counts
                int totalChargers = (int) stats.stream().mapToLong(ChargerStatisticProjection::getCount).sum();
                int availableChargers = (int) stats.stream()
                                .filter(s -> s.getStatus() == ChargerStatus.AVAILABLE)
                                .mapToLong(ChargerStatisticProjection::getCount)
                                .sum();

                // Group by connector type
                Map<String, List<ChargerStatisticProjection>> statsByType = stats.stream()
                                .collect(Collectors.groupingBy(s -> s.getType().name()));

                // Create charger summaries
                List<StationResponse.ChargerSummary> chargerSummaries = statsByType.entrySet().stream()
                                .map(entry -> {
                                        String type = entry.getKey();
                                        List<ChargerStatisticProjection> typeStats = entry.getValue();
                                        int total = (int) typeStats.stream()
                                                        .mapToLong(ChargerStatisticProjection::getCount).sum();
                                        int available = (int) typeStats.stream()
                                                        .filter(s -> s.getStatus() == ChargerStatus.AVAILABLE)
                                                        .mapToLong(ChargerStatisticProjection::getCount)
                                                        .sum();
                                        return new StationResponse.ChargerSummary(type, available, total);
                                })
                                .toList();

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
                                .availableChargersCount(availableChargers)
                                .totalChargersCount(totalChargers)
                                .chargers(chargerSummaries)
                                .openingHours(from.getOpeningHours() != null ? from.getOpeningHours().stream()
                                                .map(h -> StationOpeningHoursResponse.builder()
                                                                .id(h.getId())
                                                                .dayOfWeek(h.getDayOfWeek())
                                                                .openTime(h.getOpenTime())
                                                                .closeTime(h.getCloseTime())
                                                                .isOpen(h.getIsOpen())
                                                                .build())
                                                .toList() : null)
                                .totalPorts(totalPorts)
                                .availablePorts(availablePorts)
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
         * Alias for convert method to match naming convention.
         */
        public StationResponse toResponse(Station from) {
                return convert(from);
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
                                        int totalChargers = projection.getTotalChargersCount() != null
                                                        ? projection.getTotalChargersCount()
                                                        : 0;
                                        int availableChargers = projection.getAvailableChargersCount() != null
                                                        ? projection.getAvailableChargersCount()
                                                        : 0;

                                        // Build search result
                                        return StationSearchResult.builder()
                                                        .id(projection.getId())
                                                        // .ownerId(projection.getOwnerId())
                                                        .name(projection.getName())
                                                        // .description(projection.getDescription())
                                                        .address(projection.getAddress())
                                                        .latitude(projection.getLatitude())
                                                        .longitude(projection.getLongitude())
                                                        .rate(projection.getRate())
                                                        .status(projection.getStatus())
                                                        // .imageUrls(List.of())
                                                        .isFlaggedLowQuality(projection.getIsFlaggedLowQuality())
                                                        .distanceKm(distanceKm)
                                                        .availableChargersCount(availableChargers)
                                                        .totalChargersCount(totalChargers)
                                                        // .createdAt(projection.getCreatedAt())
                                                        // .updatedAt(projection.getUpdatedAt())
                                                        .build();
                                })
                                .collect(Collectors.toList());
        }
}
