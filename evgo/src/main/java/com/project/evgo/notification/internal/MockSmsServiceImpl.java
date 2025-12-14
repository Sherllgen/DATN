package com.project.evgo.notification.internal;

import com.project.evgo.notification.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Mock implementation of SmsService for development/testing.
 * Logs SMS content instead of actually sending.
 */
@Service
public class MockSmsServiceImpl implements SmsService {

    private static final Logger log = LoggerFactory.getLogger(MockSmsServiceImpl.class);

    @Override
    public void sendVerificationOtp(String phoneNumber, String otp) {
        log.info("========== MOCK SMS ==========");
        log.info("To: {}", phoneNumber);
        log.info("Message: Your EV-Go verification code is: {}", otp);
        log.info("===============================");
    }

    @Override
    public void sendPasswordResetOtp(String phoneNumber, String otp) {
        log.info("========== MOCK SMS ==========");
        log.info("To: {}", phoneNumber);
        log.info("Message: Your EV-Go password reset code is: {}", otp);
        log.info("===============================");
    }
}
