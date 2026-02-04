package com.project.evgo.station.request;

import jakarta.validation.constraints.*;

/**
 * Request DTO for text-based search of charging stations.
 * Searches across station name, address, and description.
 * Optionally sorts by distance if user location is provided.
 */
public record SearchTextRequest(

        @NotBlank(message = "Search query cannot be empty") @Size(min = 1, max = 255, message = "Search query must be between 1 and 255 characters") String query,

        @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90") @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90") Double latitude, // optional,
                                                                                                                                                                                    // for
                                                                                                                                                                                    // distance
                                                                                                                                                                                    // sorting

        @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180") @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180") Double longitude, // optional,
                                                                                                                                                                                             // for
                                                                                                                                                                                             // distance
                                                                                                                                                                                             // sorting

        @Positive(message = "Max results must be positive") @Max(value = 100, message = "Maximum results limit is 100") Integer maxResults // defaults
                                                                                                                                           // to
                                                                                                                                           // 20
                                                                                                                                           // if
                                                                                                                                           // null
) {
    /**
     * Constructor with defaults
     */
    public SearchTextRequest {
        if (maxResults == null) {
            maxResults = 20;
        }
    }

    /**
     * Check if location is provided for distance sorting
     */
    public boolean hasLocation() {
        return latitude != null && longitude != null;
    }
}
