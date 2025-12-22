package com.project.evgo.user.request;

import jakarta.validation.constraints.NotBlank;

public record RejectionRequest(
        @NotBlank(message = "Rejection reason is required")
        String reason
) {}
