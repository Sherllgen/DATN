package com.project.evgo.sharedkernel.infra;

import java.util.Map;

public interface FileStorageService {
    Map<String,String> generateUploadSignature();
    void deleteImage(String publicId);
}
