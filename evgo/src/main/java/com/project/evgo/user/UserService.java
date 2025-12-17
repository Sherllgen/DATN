package com.project.evgo.user;

import com.project.evgo.user.request.ChangePasswordRequest;
import com.project.evgo.user.request.UpdateProfileRequest;
import com.project.evgo.user.response.UserResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for user management.
 * Public API - accessible by other modules.
 */
public interface UserService {

    Optional<UserResponse> findById(Long id);

    List<UserResponse> findAll();

    UserResponse getCurrentUser(String email);

    UserResponse updateProfile(String email, UpdateProfileRequest request);

    void changePassword(String email, ChangePasswordRequest request);

//    UserResponse uploadAvatar(String email, MultipartFile file);

}
