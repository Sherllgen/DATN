package com.project.evgo.user.response;

import lombok.Builder;

/**
 * Response DTO for authentication operations.
 */
@Builder
public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long expiresIn,
        UserResponse user
) {
    public AuthResponse {
        if (tokenType == null) {
            tokenType = "Bearer";
        }
    }
}
