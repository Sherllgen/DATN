package com.project.evgo.user.response;

import com.project.evgo.sharedkernel.enums.StationOwnerStatus;

import java.time.LocalDateTime;

public record RegistrationResponse(
        Long registrationId,
        String registrationCode,
        String email,
        StationOwnerStatus status,
        LocalDateTime submittedAt
) {}
