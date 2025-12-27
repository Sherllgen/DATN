package com.project.evgo.sharedkernel.infra;

import com.project.evgo.user.response.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface FileStorageService {
    Map<String,String> generateUploadSignature();
    void deleteFile(String publicId);
    FileUploadResponse savePdfFile(MultipartFile file);
}
