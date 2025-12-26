package com.project.evgo.user.response;

import com.project.evgo.sharedkernel.enums.AuthProvider;
import com.project.evgo.sharedkernel.enums.UserGender;
import com.project.evgo.sharedkernel.enums.UserStatus;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for admin account view with additional fields.
 * Public API - accessible by other modules.
 */
@Builder
public record AdminAccountResponse(
        Long id,
        String email,
        String fullName,
        String phoneNumber,
        UserGender gender,
        UserStatus status,
        LocalDate birthday,
        String avatarUrl,
        List<String> roles,
        boolean emailVerified,
        boolean phoneVerified,
        AuthProvider authProvider,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
