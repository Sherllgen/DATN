package com.project.evgo.user.response;

import com.project.evgo.sharedkernel.enums.StationOwnerStatus;
import com.project.evgo.sharedkernel.enums.StationOwnerType;

import java.time.LocalDateTime;

public record PendingRegistrationResponse(
        Long profileId,
        StationOwnerType ownerType,
        String email,
        String phone,
        String name,
        LocalDateTime submittedAt
) {}