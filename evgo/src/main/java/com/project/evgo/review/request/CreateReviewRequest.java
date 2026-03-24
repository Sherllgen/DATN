package com.project.evgo.review.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a review.
 * stationId is sourced from the @PathVariable, not the request body.
 * Public API - accessible by other modules.
 */
public record CreateReviewRequest(
        @NotNull(message = "Rating is required")
        @Min(value = 1, message = "Rating must be at least 1")
        @Max(value = 5, message = "Rating must be at most 5")
        Integer rating,

        @Size(max = 1000, message = "Comment must be less than 1000 characters")
        String comment
) {
}
