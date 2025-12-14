package com.project.evgo.notification;

/**
 * Service interface for sending emails.
 * Public API - accessible by other modules.
 */
public interface EmailService {

    /**
     * Send email verification link.
     */
    void sendVerificationEmail(String email, String token);

    /**
     * Send password reset link.
     */
    void sendPasswordResetEmail(String email, String token);
}
