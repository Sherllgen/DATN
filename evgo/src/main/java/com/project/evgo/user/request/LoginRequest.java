package com.project.evgo.user.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for user login.
 * At least one of email or phoneNumber must be provided.
 */
public record LoginRequest(
    @Email(message = "Invalid email format") 
    String email,

    String phoneNumber,

    @NotBlank(message = "Password is required") 
    String password
) {
    /**
     * Get the identifier (email or phone).
     */
    public String getIdentifier() {
        return email != null && !email.isBlank() ? email : phoneNumber;
    }

    /**
     * Check if login is via email.
     */
    public boolean isEmailLogin() {
        return email != null && !email.isBlank();
    }
}
