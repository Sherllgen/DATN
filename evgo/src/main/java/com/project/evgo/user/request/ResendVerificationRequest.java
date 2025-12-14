package com.project.evgo.user.request;

/**
 * Request DTO for resending verification code.
 */
public record ResendVerificationRequest(
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
