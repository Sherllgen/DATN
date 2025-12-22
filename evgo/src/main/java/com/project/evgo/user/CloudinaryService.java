package com.project.evgo.user;

import java.io.IOException;
import java.util.Map;

public interface CloudinaryService {
    Map<String,String> generateUploadSignature();
    void deleteImage(String publicId) throws IOException;
}
