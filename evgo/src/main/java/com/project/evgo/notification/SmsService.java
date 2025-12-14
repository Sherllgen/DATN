package com.project.evgo.notification;

/**
 * Service interface for sending SMS.
 * Public API - accessible by other modules.
 */
public interface SmsService {

    /**
     * Send phone verification OTP.
     */
    void sendVerificationOtp(String phoneNumber, String otp);

    /**
     * Send password reset OTP.
     */
    void sendPasswordResetOtp(String phoneNumber, String otp);
}
