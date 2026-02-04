package com.project.evgo.station.request;

import jakarta.validation.constraints.*;

public record SearchNearbyRequest(

        @NotNull(message = "Latitude is required") @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90") @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90") Double latitude,

        @NotNull(message = "Longitude is required") @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180") @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180") Double longitude,

        @Positive(message = "Radius must be positive") @DecimalMax(value = "50.0", message = "Maximum search radius is 50 km") Double radiusKm,

        @Positive(message = "Max results must be positive") @Max(value = 100, message = "Maximum results limit is 100") Integer maxResults

) {
    /**
     * Constructor with defaults
     */
    public SearchNearbyRequest {
        if (radiusKm == null) {
            radiusKm = 5.0;
        }
        if (maxResults == null) {
            maxResults = 20;
        }
    }
}
