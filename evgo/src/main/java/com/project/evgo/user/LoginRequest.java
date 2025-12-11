package com.project.evgo.user;

/**
 * Request DTO for user login.
 * Public API - accessible by other modules.
 */
public record LoginRequest(
        String email,
        String password) {
}
