package com.project.evgo.user.response;

import com.project.evgo.sharedkernel.enums.StationOwnerType;

/**
 * Response DTO for station owner business profile.
 */
public record StationOwnerProfileResponse(
        Long id,
        StationOwnerType ownerType,
        String fullName,
        String idNumber,
        String businessName,
        String taxCode,
        String contactEmail,
        String contactPhone,
        String bankAccount,
        String bankName) {
}
