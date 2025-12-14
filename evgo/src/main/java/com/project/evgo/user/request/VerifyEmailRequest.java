package com.project.evgo.user.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for email verification with OTP.
 */
public record VerifyEmailRequest(
    @NotBlank(message = "OTP is required") 
    @Size(min = 6, max = 6, message = "OTP must be 6 digits") 
    String otp,

    @Email(message = "Invalid email format") 
    @NotBlank(message = "Email is required") 
    String email
) {
}
