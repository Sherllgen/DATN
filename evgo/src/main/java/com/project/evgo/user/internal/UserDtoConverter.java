package com.project.evgo.user.internal;

import com.project.evgo.user.response.UserResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Converter for User entity to UserResponse DTO.
 * Internal - handles entity to DTO mapping within the module.
 */
@Component
public class UserDtoConverter {

    public UserDtoConverter() {
    }

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
}
