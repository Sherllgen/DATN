package com.project.evgo.user.response;

import lombok.Builder;

@Builder
public record FileUploadResponse(
        String fileUrl,
        String publicId
) {
}
