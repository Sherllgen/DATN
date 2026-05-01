package com.project.evgo.user;

import com.project.evgo.user.request.ChangePasswordRequest;
import com.project.evgo.user.request.UpdateBusinessProfileRequest;
import com.project.evgo.user.request.UpdateProfileRequest;
import com.project.evgo.user.response.StationOwnerProfileResponse;
import com.project.evgo.user.response.UserResponse;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Service interface for user management.
 * Public API - accessible by other modules.
 */
public interface UserService {

    Optional<UserResponse> findById(Long id);

    List<UserResponse> findAll();

    List<UserResponse> findAllByIds(Set<Long> ids);

    UserResponse getCurrentUser();

    UserResponse updateProfile(UpdateProfileRequest request);

    void changePassword(ChangePasswordRequest request);

    UserResponse updateAvatar(Long userId, String avatarUrl, String publicId);

    StationOwnerProfileResponse getBusinessProfile();

    StationOwnerProfileResponse updateBusinessProfile(UpdateBusinessProfileRequest request);
}
