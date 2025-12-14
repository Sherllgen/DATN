package com.project.evgo.user.internal.token;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

/**
 * Service for managing email/phone verification tokens and OTPs.
 */
@Service
@RequiredArgsConstructor
public class VerificationTokenService {

    private static final String EMAIL_VERIFICATION_PREFIX = "verification:email:";
    private static final String PHONE_VERIFICATION_PREFIX = "verification:phone:";
    private static final String PASSWORD_RESET_PREFIX = "reset:password:";
    private static final long EMAIL_TOKEN_EXPIRY_MINUTES = 60; // 1 hour
    private static final long PHONE_OTP_EXPIRY_MINUTES = 5; // 5 minutes
    private static final long PASSWORD_RESET_EXPIRY_MINUTES = 30; // 30 minutes

    private final RedisTemplate<String, String> redisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generate and store email verification OTP.
     *
     * @param email User's email
     * @return The generated OTP (6 digits)
     */
    public String generateEmailVerificationOtp(String email) {
        String otp = generateOtp(6);
        String key = EMAIL_VERIFICATION_PREFIX + email;
        redisTemplate.opsForValue().set(key, otp, EMAIL_TOKEN_EXPIRY_MINUTES, TimeUnit.MINUTES);
        return otp;
    }

    /**
     * Validate email verification OTP.
     *
     * @param email User's email
     * @param otp   The OTP to validate
     * @return true if valid
     */
    public boolean validateEmailVerificationOtp(String email, String otp) {
        String key = EMAIL_VERIFICATION_PREFIX + email;
        String storedOtp = redisTemplate.opsForValue().get(key);
        if (storedOtp != null && storedOtp.equals(otp)) {
            redisTemplate.delete(key); // One-time use
            return true;
        }
        return false;
    }

    /**
     * Generate and store phone verification OTP.
     *
     * @param phoneNumber User's phone number
     * @return The generated OTP (6 digits)
     */
    public String generatePhoneVerificationOtp(String phoneNumber) {
        String otp = generateOtp(6);
        String key = PHONE_VERIFICATION_PREFIX + phoneNumber;
        redisTemplate.opsForValue().set(key, otp, PHONE_OTP_EXPIRY_MINUTES, TimeUnit.MINUTES);
        return otp;
    }

    /**
     * Validate phone verification OTP.
     *
     * @param phoneNumber User's phone number
     * @param otp         The OTP to validate
     * @return true if valid
     */
    public boolean validatePhoneVerificationOtp(String phoneNumber, String otp) {
        String key = PHONE_VERIFICATION_PREFIX + phoneNumber;
        String storedOtp = redisTemplate.opsForValue().get(key);
        if (storedOtp != null && storedOtp.equals(otp)) {
            redisTemplate.delete(key); // One-time use
            return true;
        }
        return false;
    }

    /**
     * Generate and store password reset token.
     *
     * @param emailOrPhone User's email or phone
     * @return The generated token
     */
    public String generatePasswordResetToken(String emailOrPhone) {
        String token = generateRandomToken(32);
        String key = PASSWORD_RESET_PREFIX + token;
        redisTemplate.opsForValue().set(key, emailOrPhone, PASSWORD_RESET_EXPIRY_MINUTES, TimeUnit.MINUTES);
        return token;
    }

    /**
     * Validate password reset token.
     *
     * @param token The token to validate
     * @return Email/phone if valid, null otherwise
     */
    public String validatePasswordResetToken(String token) {
        String key = PASSWORD_RESET_PREFIX + token;
        String emailOrPhone = redisTemplate.opsForValue().get(key);
        if (emailOrPhone != null) {
            redisTemplate.delete(key); // One-time use
        }
        return emailOrPhone;
    }

    private String generateRandomToken(int length) {
        StringBuilder sb = new StringBuilder(length);
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(secureRandom.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private String generateOtp(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(secureRandom.nextInt(10));
        }
        return sb.toString();
    }
}
