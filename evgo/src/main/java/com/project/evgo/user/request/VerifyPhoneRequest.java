package com.project.evgo.user.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for phone verification via OTP.
 */
public record VerifyPhoneRequest(
        @NotBlank(message = "OTP is required")
        @Size(min = 6, max = 6, message = "OTP must be 6 digits")
        String otp,

        @NotBlank(message = "Phone number is required")
        String phoneNumber
) {
}
