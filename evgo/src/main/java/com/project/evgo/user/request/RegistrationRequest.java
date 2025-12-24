package com.project.evgo.user.request;

import jakarta.validation.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;

public record RegistrationRequest(
        @NotBlank(message = "PDF file is required")
        MultipartFile registrationForm
) {
}
