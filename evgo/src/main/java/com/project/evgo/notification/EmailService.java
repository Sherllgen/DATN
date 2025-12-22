package com.project.evgo.notification;

import com.project.evgo.user.internal.User;

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

    void sendApprovalEmail(User user, String activationToken, String approvalMessage);
    void sendRejectionEmail(String email, String rejectionReason);
}
