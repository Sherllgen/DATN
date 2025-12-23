package com.project.evgo.user.internal.web;

import com.project.evgo.sharedkernel.dto.ApiResponse;
import com.project.evgo.user.AuthService;
import com.project.evgo.user.FileRegistrationService;
import com.project.evgo.user.request.*;
import com.project.evgo.user.response.AuthResponse;
import com.project.evgo.user.response.RegistrationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST Controller for authentication operations.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication APIs")
public class AuthController {

    private final AuthService authService;
    private final FileRegistrationService registrationService;

    @Value("${cookie.access-token.name:accessToken}")
    private String accessTokenCookieName;

    @Value("${cookie.refresh-token.name:refreshToken}")
    private String refreshTokenCookieName;

    @Value("${cookie.access-token.max-age:900}")
    private int accessTokenMaxAge;

    @Value("${cookie.refresh-token.max-age:604800}")
    private int refreshTokenMaxAge;

    @Value("${cookie.http-only:true}")
    private boolean httpOnly;

    @Value("${cookie.secure:false}")
    private boolean secure;

    @Value("${cookie.same-site:Lax}")
    private String sameSite;

    @Value("${cookie.path:/}")
    private String cookiePath;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Register with email or phone number. Verification OTP will be sent.")
    public ResponseEntity<ApiResponse<Void>> register(
            @Valid @RequestBody RegisterRequest request) {

        authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(201, "Registration successful. Please verify your account with the OTP sent.",
                        null));
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Login with email/phone and password")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {

        AuthResponse authResponse = authService.login(request);
        addTokenCookies(response, authResponse);

        return ResponseEntity.ok(new ApiResponse<>(200, "Login successful", authResponse));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Get new access token using refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @CookieValue(name = "refreshToken", required = false) String cookieRefreshToken,
            @RequestBody(required = false) RefreshTokenRequest request,
            HttpServletResponse response) {

        // Prefer cookie, fallback to request body
        String refreshToken = cookieRefreshToken != null ? cookieRefreshToken
                : (request != null ? request.refreshToken() : null);

        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Refresh token is required", null));
        }

        AuthResponse authResponse = authService.refreshToken(refreshToken);
        addTokenCookies(response, authResponse);

        return ResponseEntity.ok(new ApiResponse<>(200, "Token refreshed successfully", authResponse));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Logout and invalidate tokens")
    public ResponseEntity<ApiResponse<Void>> logout(
            HttpServletRequest request,
            HttpServletResponse response) {

        String accessToken = extractAccessToken(request);
        String refreshToken = extractRefreshToken(request);

        authService.logout(accessToken, refreshToken);
        clearTokenCookies(response);

        return ResponseEntity.ok(new ApiResponse<>(200, "Logout successful", null));
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Verify email", description = "Verify email with token")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(
            @Valid @RequestBody VerifyEmailRequest request) {

        authService.verifyEmail(request);
        return ResponseEntity.ok(new ApiResponse<>(200, "Email verified successfully", null));
    }

    @PostMapping("/verify-phone")
    @Operation(summary = "Verify phone", description = "Verify phone with OTP")
    public ResponseEntity<ApiResponse<Void>> verifyPhone(
            @Valid @RequestBody VerifyPhoneRequest request) {

        authService.verifyPhone(request);
        return ResponseEntity.ok(new ApiResponse<>(200, "Phone verified successfully", null));
    }

    @PostMapping("/resend-verification")
    @Operation(summary = "Resend verification", description = "Resend verification code")
    public ResponseEntity<ApiResponse<Void>> resendVerification(
            @Valid @RequestBody ResendVerificationRequest request) {

        authService.resendVerification(request);
        return ResponseEntity.ok(new ApiResponse<>(200, "Verification code sent", null));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot password", description = "Request password reset")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        authService.forgotPassword(request);
        return ResponseEntity.ok(new ApiResponse<>(200, "Password reset instructions sent", null));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Reset password with token")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        authService.resetPassword(request);
        return ResponseEntity.ok(new ApiResponse<>(200, "Password reset successful", null));
    }

    @PostMapping("/google")
    @Operation(summary = "Login with Google", description = "Login or register with Google OAuth")
    public ResponseEntity<ApiResponse<AuthResponse>> loginWithGoogle(
            @Valid @RequestBody GoogleLoginRequest request,
            HttpServletResponse response) {

        AuthResponse authResponse = authService.loginWithGoogle(request);
        addTokenCookies(response, authResponse);

        return ResponseEntity.ok(new ApiResponse<>(200, "Google login successful", authResponse));
    }

    @PostMapping(path = "/register/station-owner", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Submit station owner registration", description = "Submit a new station owner registration with PDF form")
    public ResponseEntity<ApiResponse<RegistrationResponse>> submitRegistration(
            @RequestParam("registrationForm") MultipartFile registrationForm) {
        RegistrationRequest request = new RegistrationRequest(registrationForm);
        RegistrationResponse response = registrationService.submitRegistration(request);
        return ResponseEntity
                .ok(new ApiResponse<>(201, "Registration submitted successfully", response));
    }

    // ------------------------ Helper methods ------------------------
    private void addTokenCookies(HttpServletResponse response, AuthResponse authResponse) {
        // Access token cookie
        Cookie accessCookie = new Cookie(accessTokenCookieName, authResponse.accessToken());
        accessCookie.setHttpOnly(httpOnly);
        accessCookie.setSecure(secure);
        accessCookie.setPath(cookiePath);
        accessCookie.setMaxAge(accessTokenMaxAge);
        response.addCookie(accessCookie);

        // Refresh token cookie
        Cookie refreshCookie = new Cookie(refreshTokenCookieName, authResponse.refreshToken());
        refreshCookie.setHttpOnly(httpOnly);
        refreshCookie.setSecure(secure);
        refreshCookie.setPath(cookiePath);
        refreshCookie.setMaxAge(refreshTokenMaxAge);
        response.addCookie(refreshCookie);
    }

    private void clearTokenCookies(HttpServletResponse response) {
        Cookie accessCookie = new Cookie(accessTokenCookieName, null);
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(secure);
        accessCookie.setPath(cookiePath);
        accessCookie.setMaxAge(0);
        response.addCookie(accessCookie);

        Cookie refreshCookie = new Cookie(refreshTokenCookieName, null);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(secure);
        refreshCookie.setPath(cookiePath);
        refreshCookie.setMaxAge(0);
        response.addCookie(refreshCookie);
    }

    private String extractAccessToken(HttpServletRequest request) {
        // Try Authorization header
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        // Try cookie
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (accessTokenCookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private String extractRefreshToken(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (refreshTokenCookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
