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
    @com.fasterxml.jackson.annotation.JsonIgnore
    public String getIdentifier() {
        return email != null && !email.isBlank() ? email : phoneNumber;
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isEmail() {
        return email != null && !email.isBlank();
    }
}
