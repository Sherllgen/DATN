package com.project.evgo.station.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Request DTO for updating a station.
 * Public API - accessible by other modules.
 */
public record UpdateStationRequest(
        @NotBlank(message = "Station name is required") 
        String name,

        String description,

        @NotBlank(message = "Address is required") 
        String address,

        @NotNull(message = "Latitude is required") 
        Double latitude,

        @NotNull(message = "Longitude is required") 
        Double longitude,

        List<String> imageUrls) {
}
