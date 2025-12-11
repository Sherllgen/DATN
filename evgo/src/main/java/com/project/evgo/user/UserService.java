package com.project.evgo.user;

import com.project.evgo.user.response.UserResponse;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for user management.
 * Public API - accessible by other modules.
 */
public interface UserService {

    Optional<UserResponse> findById(Long id);

    List<UserResponse> findAll();
}
