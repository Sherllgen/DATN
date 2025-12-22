package com.project.evgo.user.internal;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.project.evgo.sharedkernel.enums.ErrorCode;
import com.project.evgo.sharedkernel.exceptions.AppException;
import com.project.evgo.user.CloudinaryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {

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
    public void deleteImage(String publicId) {
        if (publicId != null && !publicId.isEmpty()) {
            try {
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            } catch (IOException e) {
                throw new AppException(ErrorCode.AVATAR_UPLOAD_FAILED, "Failed to delete image from Cloudinary: " + publicId);
            }
        }
    }

}
