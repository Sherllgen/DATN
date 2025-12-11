package com.project.evgo.user.response;

import com.project.evgo.sharedkernel.enums.UserGender;
import com.project.evgo.sharedkernel.enums.UserStatus;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for user information.
 * Public API - accessible by other modules.
 */
@Builder
public record UserResponse(
        Long id,
        String email,
        String fullName,
        UserGender gender,
        UserStatus status,
        LocalDate birthday,
        String avatarUrl,
        String phoneNumber,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
