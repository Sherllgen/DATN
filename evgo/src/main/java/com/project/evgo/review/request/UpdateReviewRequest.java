package com.project.evgo.review.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating a review.
 * Used for PUT /api/v1/reviews/{id}
 */
public record UpdateReviewRequest(
    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    Integer rating,

    @Size(max = 1000, message = "Comment must not exceed 1000 characters")
    String comment
) {}
