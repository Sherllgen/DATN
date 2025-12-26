package com.project.evgo.user.request;

/**
 * Request DTO for resending verification code.
 */
public record ResendVerificationRequest(
        String email,
        String phoneNumber
) {
    @com.fasterxml.jackson.annotation.JsonIgnore
    public String getIdentifier() {
        return email != null && !email.isBlank() ? email : phoneNumber;
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isEmailProvided() {
        return email != null && !email.isBlank();
    }
}
