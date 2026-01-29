package com.project.evgo.station.internal;

import com.project.evgo.station.response.StationResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Converter for Station entity to StationResponse DTO.
 * Internal - handles entity to DTO mapping within the module.
 */
@Component
public class StationDtoConverter {

    public StationDtoConverter() {
    }

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
}
