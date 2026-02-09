package com.project.evgo.user.internal;

import com.project.evgo.sharedkernel.enums.ErrorCode;
import com.project.evgo.sharedkernel.exceptions.AppException;
import com.project.evgo.sharedkernel.infra.FileStorageService;
import com.project.evgo.user.UserService;
import com.project.evgo.user.internal.token.RefreshTokenService;
import com.project.evgo.user.request.ChangePasswordRequest;
import com.project.evgo.user.request.UpdateBusinessProfileRequest;
import com.project.evgo.user.request.UpdateProfileRequest;
import com.project.evgo.user.response.StationOwnerProfileResponse;
import com.project.evgo.user.response.UserResponse;
import com.project.evgo.user.security.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of UserService.
 * Internal - not accessible by other modules.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserDtoConverter userDtoConverter;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final FileStorageService fileStorageService;
    private final StationOwnerProfileRepository stationOwnerProfileRepository;

    @Override
    public Optional<UserResponse> findById(Long id) {
        return userDtoConverter.convert(userRepository.findById(id));
    }

    @Override
    public List<UserResponse> findAll() {
        return userDtoConverter.convert(userRepository.findAll());
    }

    @Override
    public UserResponse getCurrentUser() {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return userDtoConverter.convert(user);
    }

    @Transactional
    @Override
    public UserResponse updateProfile(UpdateProfileRequest request) {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (request.fullName() != null) {
            user.setFullName(request.fullName());
        }
        if (request.phoneNumber() != null) {
            user.setPhoneNumber(request.phoneNumber());
        }
        if (request.gender() != null) {
            user.setGender(request.gender());
        }
        if (request.birthday() != null) {
            user.setBirthday(request.birthday());
        }

        User updatedUser = userRepository.save(user);
        return userDtoConverter.convert(updatedUser);
    }

    @Transactional
    @Override
    public void changePassword(ChangePasswordRequest request) {
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new AppException(ErrorCode.PASSWORD_MISMATCH);
        }

        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.CURRENT_PASSWORD_INCORRECT);
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setPasswordChangedAt(java.time.Instant.now());

        userRepository.save(user);

        // Delete all refresh tokens for this user (forces re-authentication)
        refreshTokenService.deleteAllForUser(user.getId());
    }

    private Long getCurrentUserId() {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "User not authenticated");
        }
        return userId;
    }

    @Transactional
    @Override
    public UserResponse updateAvatar(Long userId, String avatarUrl, String publicId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        // Delete old avatar from Cloudinary if exists
        if (user.getAvatarPublicId() != null && !user.getAvatarPublicId().isEmpty()) {
            fileStorageService.deleteFile(user.getAvatarPublicId());
        }

        user.setAvatarUrl(avatarUrl);
        user.setAvatarPublicId(publicId);

        userRepository.save(user);
        return userDtoConverter.convert(user);
    }

    @Override
    public StationOwnerProfileResponse getBusinessProfile() {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        StationOwnerProfile profile = stationOwnerProfileRepository.findByUser(user)
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND,
                        "Business profile not found. Only approved station owners have a business profile."));

        return convertToProfileResponse(profile);
    }

    @Transactional
    @Override
    public StationOwnerProfileResponse updateBusinessProfile(UpdateBusinessProfileRequest request) {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        StationOwnerProfile profile = stationOwnerProfileRepository.findByUser(user)
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND,
                        "Business profile not found. Only approved station owners have a business profile."));

        // Update fields if provided
        if (request.businessName() != null) {
            profile.setBusinessName(request.businessName());
        }
        if (request.taxCode() != null) {
            profile.setTaxCode(request.taxCode());
        }
        if (request.bankAccount() != null) {
            profile.setBankAccount(request.bankAccount());
        }
        if (request.bankName() != null) {
            profile.setBankName(request.bankName());
        }
        if (request.contactPhone() != null) {
            profile.setContactPhone(request.contactPhone());
        }

        StationOwnerProfile updatedProfile = stationOwnerProfileRepository.save(profile);
        log.info("Updated business profile for user: {}", userId);

        return convertToProfileResponse(updatedProfile);
    }

    private StationOwnerProfileResponse convertToProfileResponse(StationOwnerProfile profile) {
        return new StationOwnerProfileResponse(
                profile.getId(),
                profile.getOwnerType(),
                profile.getFullName(),
                profile.getIdNumber(),
                profile.getBusinessName(),
                profile.getTaxCode(),
                profile.getContactEmail(),
                profile.getContactPhone(),
                profile.getBankAccount(),
                profile.getBankName());
    }
}
