package com.project.evgo.station.internal;

import com.project.evgo.station.PortCountProvider;
import com.project.evgo.station.PortCounts;
import com.project.evgo.station.response.StationOpeningHoursResponse;
import com.project.evgo.station.response.StationResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Converter for Station entity to StationResponse DTO.
 * Internal - handles entity to DTO mapping within the module.
 */
@Component
@RequiredArgsConstructor
public class StationDtoConverter {

    private final PortCountProvider portCountProvider;

    public StationResponse convert(Station from) {
        // Calculate port counts using provider interface
        PortCounts counts = portCountProvider.getPortCounts(from.getId());
        int totalPorts = counts.totalPorts();
        int availablePorts = counts.availablePorts();

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
}
