package com.project.evgo.user.response;

import com.project.evgo.sharedkernel.enums.StationOwnerStatus;
import com.project.evgo.sharedkernel.enums.StationOwnerType;

import java.time.LocalDateTime;

public record RegistrationAdminResponse(
        Long profileId,
        String registrationCode,
        StationOwnerType ownerType,
        String email,
        String phone,
        String name,
        String pdfFileUrl,
        StationOwnerStatus status,
        LocalDateTime submittedAt
) {}