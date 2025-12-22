package com.project.evgo.user.response;

public record UploadSignatureResponse(
        String cloudName,
        String apiKey,
        String timestamp,
        String signature,
        String folder
) {
}
