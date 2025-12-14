package com.project.evgo.user.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for user registration.
 * At least one of email or phoneNumber must be provided.
 */
public record RegisterRequest(
    @Email(message = "Invalid email format") 
    String email,

    String phoneNumber,

    @NotBlank(message = "Password is required") 
    @Size(min = 8, message = "Password must be at least 8 characters") 
    String password,

    @NotBlank(message = "Full name is required") String fullName) 
{
    /**
     * Validate that at least email or phone is provided.
     */
    public boolean isValid() {
        return (email != null && !email.isBlank()) || (phoneNumber != null && !phoneNumber.isBlank());
    }
}
