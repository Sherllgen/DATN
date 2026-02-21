package com.project.evgo.station.internal;

import com.project.evgo.station.response.StationPhotoResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Converter for StationPhoto entity to StationPhotoResponse DTO.
 * Internal - handles entity to DTO mapping within the module.
 */
@Component
public class StationPhotoDtoConverter {

    public StationPhotoDtoConverter() {
    }

    public StationPhotoResponse convert(StationPhoto from) {
        return StationPhotoResponse.builder()
                .id(from.getId())
                .stationId(from.getStationId())
                .imageUrl(from.getImageUrl())
                .caption(from.getCaption())
                .displayOrder(from.getDisplayOrder())
                .createdAt(from.getCreatedAt())
                .build();
    }

    public List<StationPhotoResponse> convert(List<StationPhoto> from) {
        return from.stream().map(this::convert).toList();
    }

    public Optional<StationPhotoResponse> convert(Optional<StationPhoto> from) {
        return from.map(this::convert);
    }
}
