package com.project.evgo.user.internal;

import com.project.evgo.user.response.AdminAccountResponse;
import com.project.evgo.user.response.UserResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Converter for User entity to DTO.
 * Internal - handles entity to DTO mapping within the module.
 */
@Component
public class UserDtoConverter {

    public UserDtoConverter() {
    }

    // ==================== UserResponse conversions ====================

    public UserResponse convert(User from) {
        return UserResponse.builder()
                .id(from.getId())
                .email(from.getEmail())
                .fullName(from.getFullName())
                .gender(from.getGender())
                .status(from.getStatus())
                .birthday(from.getBirthday())
                .avatarUrl(from.getAvatarUrl())
                .phoneNumber(from.getPhoneNumber())
                .roles(from.getRoles().stream().map(Role::getName).toList())
                .createdAt(from.getCreatedAt())
                .updatedAt(from.getUpdatedAt())
                .build();
    }

    public List<UserResponse> convert(List<User> from) {
        return from.stream().map(this::convert).toList();
    }

    public Optional<UserResponse> convert(Optional<User> from) {
        return from.map(this::convert);
    }

    // ==================== AdminAccountResponse conversions ====================

    public AdminAccountResponse convertToAdmin(User from) {
        List<String> roleNames = from.getRoles().stream()
                .map(Role::getName)
                .toList();

        return AdminAccountResponse.builder()
                .id(from.getId())
                .email(from.getEmail())
                .fullName(from.getFullName())
                .phoneNumber(from.getPhoneNumber())
                .gender(from.getGender())
                .status(from.getStatus())
                .birthday(from.getBirthday())
                .avatarUrl(from.getAvatarUrl())
                .roles(roleNames)
                .emailVerified(from.isEmailVerified())
                .phoneVerified(from.isPhoneVerified())
                .authProvider(from.getAuthProvider())
                .createdAt(from.getCreatedAt())
                .updatedAt(from.getUpdatedAt())
                .build();
    }

    public List<AdminAccountResponse> convertToAdminList(List<User> from) {
        return from.stream().map(this::convertToAdmin).toList();
    }

    public Optional<AdminAccountResponse> convertToAdminOptional(Optional<User> from) {
        return from.map(this::convertToAdmin);
    }
}
