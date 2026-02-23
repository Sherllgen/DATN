package com.project.evgo.sharedkernel.infra;

import com.project.evgo.sharedkernel.dto.FileUploadResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface FileStorageService {
    Map<String, String> generateUploadSignature();

    void deleteFile(String publicId);

    FileUploadResult savePdfFile(MultipartFile file);

    FileUploadResult saveImageFile(MultipartFile file, String folder);
}
