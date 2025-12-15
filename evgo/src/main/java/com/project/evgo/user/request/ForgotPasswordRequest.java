package com.project.evgo.user.request;

import jakarta.validation.constraints.Email;

/**
 * Request DTO for forgot password.
 * At least one of email or phoneNumber must be provided.
 */
public record ForgotPasswordRequest(
        @Email(message = "Invalid email format")
        String email,

        String phoneNumber
) {
    public String getIdentifier() {
        return email != null && !email.isBlank() ? email : phoneNumber;
    }

    public boolean isEmail() {
        return email != null && !email.isBlank();
    }
}
