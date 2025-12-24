package com.project.evgo.sharedkernel.infra;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface FileStorageService {
    Map<String,String> generateUploadSignature();
    void deleteImage(String publicId);
    String savePdfFile(MultipartFile file);
}
