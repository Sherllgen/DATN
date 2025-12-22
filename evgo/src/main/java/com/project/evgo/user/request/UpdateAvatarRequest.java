package com.project.evgo.user.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateAvatarRequest(
        @NotBlank(message = "Avatar URL is required")
        String avatarUrl,

        @NotBlank(message = "Public ID is required")
        String publicId
) {
}
