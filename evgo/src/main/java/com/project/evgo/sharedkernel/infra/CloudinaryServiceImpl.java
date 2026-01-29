package com.project.evgo.sharedkernel.infra;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.project.evgo.sharedkernel.dto.FileUploadResult;
import com.project.evgo.sharedkernel.enums.ErrorCode;
import com.project.evgo.sharedkernel.exceptions.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryServiceImpl implements FileStorageService {

    private final Cloudinary cloudinary;

    @Override
    public Map<String, String> generateUploadSignature() {
        long timestamp = System.currentTimeMillis() / 1000;
        String folder = "users/avatars";

        Map<String, Object> params = new HashMap<>();
        params.put("timestamp", timestamp);
        params.put("folder", folder);

        String signature = cloudinary.apiSignRequest(params, cloudinary.config.apiSecret);

        Map<String, String> result = new HashMap<>();
        result.put("signature", signature);
        result.put("timestamp", String.valueOf(timestamp));
        result.put("apiKey", cloudinary.config.apiKey);
        result.put("cloudName", cloudinary.config.cloudName);
        result.put("folder", folder);

        return result;
    }

    @Override
    public void deleteFile(String publicId) {
        if (publicId != null && !publicId.isEmpty()) {
            try {
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            } catch (IOException e) {
                throw new AppException(ErrorCode.AVATAR_UPLOAD_FAILED,
                        "Failed to delete image from Cloudinary: " + publicId);
            }
        }
    }

    @Override
    public FileUploadResult savePdfFile(MultipartFile file) {
        try {
            Map<String, Object> uploadParams = new HashMap<>();
            uploadParams.put("resource_type", "image");
            uploadParams.put("folder", "registration-forms");
            uploadParams.put("public_id", UUID.randomUUID().toString());
            uploadParams.put("format", "pdf");

            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    uploadParams);

            String cloudinaryUrl = (String) uploadResult.get("secure_url");
            log.info("PDF file uploaded to Cloudinary successfully: {}", cloudinaryUrl);

            String publicId = (String) uploadResult.get("public_id");

            return FileUploadResult.builder()
                    .fileUrl(cloudinaryUrl)
                    .publicId(publicId)
                    .build();

        } catch (IOException e) {
            log.error("Failed to upload PDF file to Cloudinary: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED, "Failed to upload PDF file to Cloudinary");
        }
    }

}
