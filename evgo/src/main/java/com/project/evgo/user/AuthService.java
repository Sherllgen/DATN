package com.project.evgo.user;

import com.project.evgo.user.request.*;
import com.project.evgo.user.response.AuthResponse;

/**
 * Service interface for authentication operations.
 * Public API - accessible by other modules.
 */
public interface AuthService {

    /**
     * Register a new user with email or phone number.
     * User must verify before login.
     */
    void register(RegisterRequest request);

    /**
     * Login with email/phone and password.
     */
    AuthResponse login(LoginRequest request);

    /**
     * Refresh access token using refresh token.
     */
    AuthResponse refreshToken(String refreshToken);

    /**
     * Logout user and invalidate tokens.
     */
    void logout(String accessToken, String refreshToken);

    /**
     * Verify email with token.
     */
    void verifyEmail(VerifyEmailRequest request);

    /**
     * Verify phone with OTP.
     */
    void verifyPhone(VerifyPhoneRequest request);

    /**
     * Resend verification code to email or phone.
     */
    void resendVerification(ResendVerificationRequest request);

    /**
     * Request password reset.
     */
    void forgotPassword(ForgotPasswordRequest request);

    /**
     * Reset password with token.
     */
    void resetPassword(ResetPasswordRequest request);

    /**
     * Login with Google OAuth.
     */
    AuthResponse loginWithGoogle(GoogleLoginRequest request);
}
