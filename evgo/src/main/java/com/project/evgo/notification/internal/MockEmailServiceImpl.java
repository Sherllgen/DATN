package com.project.evgo.notification.internal;

import com.project.evgo.notification.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Mock implementation of EmailService for development/testing.
 * Logs email content instead of actually sending.
 */
@Service
public class MockEmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(MockEmailServiceImpl.class);

    @Override
    public void sendVerificationEmail(String email, String token) {
        log.info("========== MOCK EMAIL ==========");
        log.info("To: {}", email);
        log.info("Subject: Verify your EV-Go account");
        log.info("Verification Token: {}", token);
        log.info("Verification Link: http://localhost:8080/api/v1/auth/verify-email?token={}&email={}", token, email);
        log.info("=================================");
    }

    @Override
    public void sendPasswordResetEmail(String email, String token) {
        log.info("========== MOCK EMAIL ==========");
        log.info("To: {}", email);
        log.info("Subject: Reset your EV-Go password");
        log.info("Reset Token: {}", token);
        log.info("Reset Link: http://localhost:8080/reset-password?token={}", token);
        log.info("=================================");
    }
}
