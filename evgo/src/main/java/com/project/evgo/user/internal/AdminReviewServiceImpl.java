package com.project.evgo.user.internal;

import com.project.evgo.notification.EmailService;
import com.project.evgo.sharedkernel.dto.PageResponse;
import com.project.evgo.sharedkernel.enums.ErrorCode;
import com.project.evgo.sharedkernel.enums.StationOwnerStatus;
import com.project.evgo.sharedkernel.enums.StationOwnerType;
import com.project.evgo.sharedkernel.enums.UserStatus;
import com.project.evgo.sharedkernel.exceptions.AppException;
import com.project.evgo.user.AdminReviewService;
import com.project.evgo.user.request.RejectionRequest;
import com.project.evgo.user.response.PendingRegistrationResponse;
import com.project.evgo.user.response.RegistrationDetailResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminReviewServiceImpl implements AdminReviewService {

    private final StationOwnerProfileRepository stationOwnerProfileRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PendingRegistrationResponse> getPendingRegistrations(Pageable pageable) {
        Page<StationOwnerProfile> profiles = stationOwnerProfileRepository
                .findByStatus(StationOwnerStatus.PENDING, pageable);

        Page<PendingRegistrationResponse> responsePage = profiles.map(profile -> new PendingRegistrationResponse(
                profile.getId(),
                profile.getOwnerType(),
                profile.getEmail(),
                profile.getPhone(),
                profile.getOwnerType() == StationOwnerType.INDIVIDUAL
                        ? profile.getFullName()
                        : profile.getBusinessName(),
                profile.getPdfFilePath(),
                profile.getSubmittedAt()
        ));

        return PageResponse.of(responsePage);
    }


    @Override
    @Transactional(readOnly = true)
    public RegistrationDetailResponse getRegistrationDetail(Long profileId) {
        StationOwnerProfile profile = stationOwnerProfileRepository.findById(profileId)
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND,
                        "Station owner profile not found"));

        return new RegistrationDetailResponse(
                profile.getId(),
                profile.getOwnerType(),
                profile.getFullName(),
                profile.getIdNumber(),
                profile.getBusinessName(),
                profile.getTaxCode(),
                profile.getEmail(),
                profile.getPhone(),
                profile.getBankAccount(),
                profile.getBankName(),
                profile.getStatus(),
                profile.getPdfFilePath(),
                profile.getSubmittedAt(),
                profile.getReviewedAt(),
                profile.getRejectionReason()
        );
    }

    @Override
    @Transactional
    public void approveRegistration(Long profileId) {
        StationOwnerProfile profile = stationOwnerProfileRepository.findById(profileId)
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND,
                        "Station owner profile not found"));

        if (profile.getStatus() != StationOwnerStatus.PENDING) {
            throw new AppException(ErrorCode.INVALID_STATUS,
                    "Only pending registrations can be approved");
        }

        String generatedPassword = generateRandomPassword(10);
        User user = createUserFromProfile(profile, generatedPassword);

        profile.setUser(user);
        profile.setStatus(StationOwnerStatus.APPROVED);
        profile.setReviewedAt(LocalDateTime.now());
        stationOwnerProfileRepository.save(profile);

        emailService.sendApprovalEmailWithPassword(
                user.getEmail(),
                user.getFullName(),
                generatedPassword
        );

        log.info("Registration approved for profile ID: {} with auto-generated password", profileId);
    }


    @Override
    @Transactional
    public void rejectRegistration(Long profileId, RejectionRequest request) {
        StationOwnerProfile profile = stationOwnerProfileRepository.findById(profileId)
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND,
                        "Station owner profile not found"));

        if (profile.getStatus() != StationOwnerStatus.PENDING) {
            throw new AppException(ErrorCode.INVALID_STATUS,
                    "Only pending registrations can be rejected");
        }

        profile.setStatus(StationOwnerStatus.REJECTED);
        profile.setRejectionReason(request.reason());
        profile.setReviewedAt(LocalDateTime.now());
        stationOwnerProfileRepository.save(profile);

        emailService.sendRejectionEmail(profile.getEmail(), request.reason());

        log.info("Registration rejected for profile ID: {}", profileId);
    }

    private User createUserFromProfile(StationOwnerProfile profile, String rawPassword) {
        User user = new User();
        user.setEmail(profile.getEmail());
        user.setPhoneNumber(profile.getPhone());
        user.setPassword(passwordEncoder.encode(rawPassword));

        String fullName = profile.getOwnerType() == StationOwnerType.ENTERPRISE
                ? profile.getBusinessName()
                : profile.getFullName();
        user.setFullName(fullName);

        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerified(true);

        Role stationOwnerRole = roleRepository.findByName("STATION_OWNER")
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND,
                        "Station owner role not found"));
        user.setRoles(new HashSet<>());
        user.getRoles().add(stationOwnerRole);

        return userRepository.save(user);
    }

    private String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        java.util.Random random = new java.util.Random();

        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        return password.toString();
    }
}
