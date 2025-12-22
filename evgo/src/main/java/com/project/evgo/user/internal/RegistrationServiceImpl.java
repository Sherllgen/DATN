package com.project.evgo.user.internal;

import com.cloudinary.Cloudinary;
import com.project.evgo.sharedkernel.enums.ErrorCode;
import com.project.evgo.sharedkernel.exceptions.AppException;
import com.project.evgo.user.PdfParsingService;
import com.project.evgo.user.RegistrationService;
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
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {

    private final StationOwnerProfileRepository stationOwnerProfileRepository;
    private final PdfParsingService pdfParsingService;

    private final Cloudinary cloudinary;

    @Value("${app.upload.max-size}")
    private long maxFileSize;

    @Override
    @Transactional
    public RegistrationResponse submitRegistration(RegistrationRequest request) {
        validatePdfFile(request.registrationForm());

        StationOwnerProfile profile = pdfParsingService.parseRegistrationPdf(request.registrationForm());

        checkExistingProfile(profile);

        String pdfFilePath = savePdfFile(request.registrationForm());
        profile.setPdfFilePath(pdfFilePath);

        StationOwnerProfile savedProfile = stationOwnerProfileRepository.save(profile);

        log.info("Registration submitted successfully for: {}", profile.getEmail());

        return new RegistrationResponse(
                savedProfile.getId(),
                savedProfile.getEmail(),
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
        if (stationOwnerProfileRepository.existsByEmail(profile.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS, "Email is already registered");
        }

        if (profile.getPhone() != null && stationOwnerProfileRepository.existsByPhone(profile.getPhone())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS, "Phone number is already registered");
        }
    }

    private String savePdfFile(MultipartFile file) {
        try {
            Map<String, Object> uploadParams = new HashMap<>();
            uploadParams.put("resource_type", "raw");
            uploadParams.put("folder", "registration-forms");
            uploadParams.put("public_id", UUID.randomUUID().toString());
            uploadParams.put("format", "pdf");

            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    uploadParams
            );

            String cloudinaryUrl = (String) uploadResult.get("secure_url");
            log.info("PDF file uploaded to Cloudinary successfully: {}", cloudinaryUrl);

            return cloudinaryUrl;

        } catch (IOException e) {
            log.error("Failed to upload PDF file to Cloudinary: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED, "Failed to upload PDF file to Cloudinary");
        }
    }

}
