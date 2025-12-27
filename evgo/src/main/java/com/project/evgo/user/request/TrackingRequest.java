package com.project.evgo.user.request;

import jakarta.validation.constraints.Email;

public record TrackingRequest(
        @Email String email,
        String registrationCode
) {
}
