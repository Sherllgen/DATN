package com.project.evgo.user.internal;

import com.cloudinary.Cloudinary;
import com.project.evgo.sharedkernel.enums.ErrorCode;
import com.project.evgo.sharedkernel.enums.StationOwnerStatus;
import com.project.evgo.sharedkernel.exceptions.AppException;
import com.project.evgo.sharedkernel.infra.FileStorageService;
import com.project.evgo.user.PdfParsingService;
import com.project.evgo.user.FileRegistrationService;
import com.project.evgo.user.request.RegistrationRequest;
import com.project.evgo.user.response.RegistrationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileRegistrationServiceImpl implements FileRegistrationService {

    private final StationOwnerProfileRepository stationOwnerProfileRepository;
    private final UserRepository userRepository;
    private final PdfParsingService pdfParsingService;
    private final FileStorageService fileStorageService;

    @Value("${app.upload.max-size}")
    private long maxFileSize;

    @Override
    @Transactional
    public RegistrationResponse submitRegistration(RegistrationRequest request) {
        //1. Validate PDF file is present and within size limits
        validatePdfFile(request.registrationForm());
        //2. Parse PDF to extract form data
        StationOwnerProfile profile = pdfParsingService.parseRegistrationPdf(request.registrationForm());
        String contactEmail = profile.getContactEmail();
        //3. Check for existing profiles with same email or phone
        Optional<StationOwnerProfile> existingProfileOpt = stationOwnerProfileRepository.findByContactEmail(contactEmail);

        StationOwnerProfile finalProfile;
        if (existingProfileOpt.isPresent()) {
            StationOwnerProfile existingProfile = existingProfileOpt.get();

            if (existingProfile.getStatus() == StationOwnerStatus.APPROVED) {
                throw new AppException(ErrorCode.RESOURCE_ALREADY_EXISTS,
                        "Email is already registered with an approved profile");
            }

//            profile.setStatus(StationOwnerStatus.PENDING);
            updateProfileData(existingProfile, profile);

            existingProfile.setStatus(StationOwnerStatus.PENDING);
            existingProfile.setRejectionReason(null);

            finalProfile = existingProfile;
        } else {
            // Check email/phone conflict with existing users
            checkExistingProfile(profile);

//            profile.setStatus(StationOwnerStatus.PENDING);
            finalProfile = profile;
            finalProfile.setStatus(StationOwnerStatus.PENDING);
        }

        String pdfFilePath = fileStorageService.savePdfFile(request.registrationForm());
//        profile.setPdfFilePath(pdfFilePath);
//
//        StationOwnerProfile savedProfile = stationOwnerProfileRepository.save(profile);
        finalProfile.setPdfFilePath(pdfFilePath);

        StationOwnerProfile savedProfile = stationOwnerProfileRepository.save(finalProfile);

        log.info("Registration submitted successfully for: {}", profile.getContactEmail());

        return new RegistrationResponse(
                savedProfile.getId(),
                savedProfile.getContactEmail(),
                savedProfile.getStatus(),
                savedProfile.getSubmittedAt()
        );
    }

    private void validatePdfFile(org.springframework.web.multipart.MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Registration PDF form is required");
        }

        if (file.getSize() > maxFileSize) {
            throw new AppException(ErrorCode.FILE_TOO_LARGE,
                    String.format("File size exceeds maximum allowed size of %d bytes", maxFileSize));
        }
    }

    private void checkExistingProfile(StationOwnerProfile profile) {
        if (userRepository.existsByEmail(profile.getContactEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS, "Email is already registered");
        }

        if (profile.getContactPhone() != null && userRepository.existsByPhoneNumber(profile.getContactPhone())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS, "Phone number is already registered");
        }
    }

    private void updateProfileData(StationOwnerProfile target, StationOwnerProfile source) {
        target.setFullName(source.getFullName());
        target.setIdNumber(source.getIdNumber());
        target.setOwnerType(source.getOwnerType());

        target.setBusinessName(source.getBusinessName());
        target.setTaxCode(source.getTaxCode());

        target.setContactPhone(source.getContactPhone());

        target.setBankAccount(source.getBankAccount());
        target.setBankName(source.getBankName());
    }

}
