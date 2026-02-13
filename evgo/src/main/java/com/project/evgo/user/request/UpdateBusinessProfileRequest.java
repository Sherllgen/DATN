package com.project.evgo.user.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating station owner business profile.
 */
public record UpdateBusinessProfileRequest(
        @Size(max = 200, message = "Business name must not exceed 200 characters") 
        String businessName,

        @Size(max = 20, message = "Tax code must not exceed 20 characters") 
        String taxCode,

        @Size(max = 30, message = "Bank account must not exceed 30 characters") 
        String bankAccount,

        @Size(max = 100, message = "Bank name must not exceed 100 characters") 
        String bankName,

        @Pattern(regexp = "^(\\+84|0)[0-9]{9}$", message = "Invalid phone number format") 
        String contactPhone) {
}
