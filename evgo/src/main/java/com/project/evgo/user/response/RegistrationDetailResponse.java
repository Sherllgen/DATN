package com.project.evgo.user.response;

import com.project.evgo.sharedkernel.enums.StationOwnerStatus;
import com.project.evgo.sharedkernel.enums.StationOwnerType;

import java.time.LocalDateTime;

public record RegistrationDetailResponse(
        Long id,
        StationOwnerType ownerType,
        String fullName,
        String idNumber,
        String businessName,
        String taxCode,
        String email,
        String phone,
        String bankAccount,
        String bankName,
        StationOwnerStatus status,
        String pdfFilePath,
        LocalDateTime submittedAt,
        LocalDateTime reviewedAt,
        String rejectionReason
) {
}
