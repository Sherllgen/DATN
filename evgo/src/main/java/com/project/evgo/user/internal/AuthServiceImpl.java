package com.project.evgo.user.internal;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.project.evgo.user.security.JwtTokenProvider;
import com.project.evgo.notification.EmailService;
import com.project.evgo.notification.SmsService;
import com.project.evgo.sharedkernel.enums.AuthProvider;
import com.project.evgo.sharedkernel.enums.ErrorCode;
import com.project.evgo.sharedkernel.enums.UserStatus;
import com.project.evgo.sharedkernel.exceptions.AppException;
import com.project.evgo.user.AuthService;
import com.project.evgo.user.internal.token.RefreshTokenService;
import com.project.evgo.user.internal.token.TokenBlacklistService;
import com.project.evgo.user.internal.token.VerificationTokenService;
import com.project.evgo.user.request.*;
import com.project.evgo.user.response.AuthResponse;
import com.project.evgo.user.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of AuthService.
 * Internal - not accessible by other modules.
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    private static final String DEFAULT_USER_ROLE = "USER";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final TokenBlacklistService tokenBlacklistService;
    private final VerificationTokenService verificationTokenService;
    private final EmailService emailService;
    private final SmsService smsService;
    private final UserDtoConverter userDtoConverter;

    @Value("${jwt.refresh-token.expiration}")
    private Long refreshTokenExpiration;

    @Value("${google.client-id:}")
    private String googleClientId;

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        // Validate email or phone provided
        if (!request.isValid()) {
            throw new AppException(ErrorCode.EMAIL_OR_PHONE_REQUIRED);
        }

        // Check if email already exists
        if (request.email() != null && !request.email().isBlank()) {
            Optional<User> userOpt = userRepository.findByEmail(request.email());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (user.getStatus().equals(UserStatus.INACTIVE)) {
                    throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS,
                            "Email already registered but not verified. Please verify your email or use a different one.");
                } else {
                    throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
                }
            }
            // if (userRepository.existsByEmail(request.email())) {
            //     throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
            // }
        }

        // Check if phone already exists
        if (request.phoneNumber() != null && !request.phoneNumber().isBlank()) {
            if (userRepository.existsByPhoneNumber(request.phoneNumber())) {
                throw new AppException(ErrorCode.PHONE_ALREADY_EXISTS);
            }
        }

        // Find default USER role
        Role userRole = roleRepository.findByName(DEFAULT_USER_ROLE)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND, "Default role not found"));

        // Create new user
        User user = new User();
        user.setEmail(request.email());
        user.setPhoneNumber(request.phoneNumber());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName());
        user.setStatus(UserStatus.INACTIVE);
        user.setRoles(Set.of(userRole));
        user.setEmailVerified(false);
        user.setPhoneVerified(false);
        user.setAuthProvider(AuthProvider.LOCAL);

        user = userRepository.save(user);
        log.info("User registered successfully: {}", user.getId());

        // Send verification OTP
        if (request.email() != null && !request.email().isBlank()) {
            String otp = verificationTokenService.generateEmailVerificationOtp(request.email());
            emailService.sendVerificationEmail(request.email(), otp);
        } else if (request.phoneNumber() != null && !request.phoneNumber().isBlank()) {
            String otp = verificationTokenService.generatePhoneVerificationOtp(request.phoneNumber());
            smsService.sendVerificationOtp(request.phoneNumber(), otp);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        // Find user by email or phone
        User user = findUserByIdentifier(request.getIdentifier());

        // Validate password
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        // Check if account is verified
        if (!user.isEmailVerified() && !user.isPhoneVerified()) {
            throw new AppException(ErrorCode.ACCOUNT_NOT_VERIFIED);
        }

        // Check if account is active
        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new AppException(ErrorCode.ACCOUNT_DISABLED);
        }

        log.info("User logged in successfully: {}", user.getId());
        return generateAuthResponse(user);
    }

    @Override
    @Transactional
    public AuthResponse loginWithGoogle(GoogleLoginRequest request) {
        // Verify Google ID token
        GoogleIdToken.Payload payload = verifyGoogleToken(request.idToken());

        String email = payload.getEmail();
        String providerId = payload.getSubject();
        String name = (String) payload.get("name");
        String pictureUrl = (String) payload.get("picture");

        // Find or create user
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> createGoogleUser(email, providerId, name, pictureUrl));

        // Update provider info if existing user switches to Google
        if (user.getAuthProvider() == AuthProvider.LOCAL) {
            user.setAuthProvider(AuthProvider.GOOGLE);
            user.setProviderId(providerId);
            if (pictureUrl != null && user.getAvatarUrl() == null) {
                user.setAvatarUrl(pictureUrl);
            }
            userRepository.save(user);
        }

        log.info("User logged in with Google: {}", user.getId());
        return generateAuthResponse(user);
    }

    private GoogleIdToken.Payload verifyGoogleToken(String idToken) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    // .setIssuer("https://accounts.google.com") // Handle both with/without https
                    // if needed later
                    .build();

            GoogleIdToken googleIdToken = verifier.verify(idToken);
            if (googleIdToken == null) {
                log.error("GoogleIdTokenVerifier returned null. Invalid signature or claims.");

                throw new AppException(ErrorCode.INVALID_TOKEN, "Invalid Google ID token");
            }

            return googleIdToken.getPayload();
        } catch (Exception e) {
            log.error("Failed to verify Google token. Error: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.INVALID_TOKEN, "Failed to verify Google token: " + e.getMessage());
        }
    }

    private User createGoogleUser(String email, String providerId, String name, String pictureUrl) {
        Role userRole = roleRepository.findByName(DEFAULT_USER_ROLE)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND, "Default role not found"));

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setFullName(name != null ? name : "Google User");
        user.setStatus(UserStatus.ACTIVE);
        user.setRoles(Set.of(userRole));
        user.setEmailVerified(true); // Google emails are verified
        user.setPhoneVerified(false);
        user.setAuthProvider(AuthProvider.GOOGLE);
        user.setProviderId(providerId);
        user.setAvatarUrl(pictureUrl);

        user = userRepository.save(user);
        log.info("Created new user from Google: {}", user.getId());
        return user;
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        // Validate refresh token
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }

        // Check token type
        if (!"refresh".equals(jwtTokenProvider.getTokenType(refreshToken))) {
            throw new AppException(ErrorCode.INVALID_TOKEN, "Not a refresh token");
        }

        // Validate token in Redis
        Long userId = refreshTokenService.validate(refreshToken);
        if (userId == null) {
            throw new AppException(ErrorCode.INVALID_TOKEN, "Refresh token not found or expired");
        }

        // Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "User not found"));

        // Delete old refresh token
        refreshTokenService.delete(refreshToken);

        log.info("Token refreshed for user: {}", userId);
        return generateAuthResponse(user);
    }

    @Override
    public void logout(String accessToken, String refreshToken) {
        // Blacklist access token
        if (accessToken != null) {
            long remainingTime = jwtTokenProvider.getRemainingTime(accessToken);
            tokenBlacklistService.blacklist(accessToken, remainingTime);
        }

        // Delete refresh token
        if (refreshToken != null) {
            refreshTokenService.delete(refreshToken);
        }

        log.info("User logged out successfully");
    }

    @Override
    @Transactional
    public void verifyEmail(VerifyEmailRequest request) {
        // Validate OTP
        boolean valid = verificationTokenService.validateEmailVerificationOtp(
                request.email(), request.otp());
        if (!valid) {
            throw new AppException(ErrorCode.INVALID_TOKEN, "Invalid or expired OTP");
        }

        // Update user
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "User not found"));

        user.setEmailVerified(true);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        log.info("Email verified for user: {}", user.getId());
    }

    @Override
    @Transactional
    public void verifyPhone(VerifyPhoneRequest request) {
        // Validate OTP
        boolean valid = verificationTokenService.validatePhoneVerificationOtp(
                request.phoneNumber(), request.otp());
        if (!valid) {
            throw new AppException(ErrorCode.INVALID_TOKEN, "Invalid or expired OTP");
        }

        // Update user
        User user = userRepository.findByPhoneNumber(request.phoneNumber())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "User not found"));

        user.setPhoneVerified(true);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        log.info("Phone verified for user: {}", user.getId());
    }

    @Override
    public void resendVerification(ResendVerificationRequest request) {
        if (request.isEmailProvided()) {
            User user = userRepository.findByEmail(request.email())
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "User not found"));

            if (user.isEmailVerified()) {
                throw new AppException(ErrorCode.INVALID_REQUEST, "Email already verified");
            }

            String otp = verificationTokenService.generateEmailVerificationOtp(request.email());
            emailService.sendVerificationEmail(request.email(), otp);
        } else {
            User user = userRepository.findByPhoneNumber(request.phoneNumber())
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "User not found"));

            if (user.isPhoneVerified()) {
                throw new AppException(ErrorCode.INVALID_REQUEST, "Phone already verified");
            }

            String otp = verificationTokenService.generatePhoneVerificationOtp(request.phoneNumber());
            smsService.sendVerificationOtp(request.phoneNumber(), otp);
        }

        log.info("Verification resent to: {}", request.getIdentifier());
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        User user;
        if (request.isEmailProvided()) {
            user = userRepository.findByEmail(request.email())
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "User not found"));

            String token = verificationTokenService.generatePasswordResetToken(request.email());
            emailService.sendPasswordResetEmail(request.email(), token);
        } else {
            user = userRepository.findByPhoneNumber(request.phoneNumber())
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "User not found"));

            String otp = verificationTokenService.generatePhoneVerificationOtp(request.phoneNumber());
            smsService.sendPasswordResetOtp(request.phoneNumber(), otp);
        }

        log.info("Password reset requested for user: {}", user.getId());
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        // Validate passwords match
        if (!request.passwordsMatch()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Passwords do not match");
        }

        // Validate token
        String identifier = verificationTokenService.validatePasswordResetToken(request.token());
        if (identifier == null) {
            throw new AppException(ErrorCode.INVALID_TOKEN, "Invalid or expired reset token");
        }

        // Find user
        User user = userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByPhoneNumber(identifier))
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "User not found"));

        // Update password
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        // Invalidate all sessions
        refreshTokenService.deleteAllForUser(user.getId());

        log.info("Password reset for user: {}", user.getId());
    }

    private User findUserByIdentifier(String identifier) {
        return userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByPhoneNumber(identifier))
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_CREDENTIALS));
    }

    private AuthResponse generateAuthResponse(User user) {
        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail(), roles);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        // Store refresh token in Redis
        refreshTokenService.store(user.getId(), refreshToken, refreshTokenExpiration);

        UserResponse userResponse = userDtoConverter.convert(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpirationSeconds())
                .user(userResponse)
                .build();
    }
}
