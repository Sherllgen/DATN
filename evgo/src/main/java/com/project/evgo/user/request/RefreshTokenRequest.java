package com.project.evgo.user.request;

/**
 * Request DTO for refreshing token.
 */
public record RefreshTokenRequest(
    String refreshToken
) {
}
