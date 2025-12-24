package com.project.evgo.user.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request for Google OAuth login.
 */
public record GoogleLoginRequest(
        @NotBlank(message = "ID token is required") String idToken) {
}
