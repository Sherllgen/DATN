package com.project.evgo.station.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for creating/updating a station.
 * Public API - accessible by other modules.
 */
public record CreateStationRequest(
        @NotBlank(message = "Station name is required") String name,

        @NotBlank(message = "Address is required") String address,

        @NotNull(message = "Latitude is required") Double latitude,

        @NotNull(message = "Longitude is required") Double longitude) {
}
