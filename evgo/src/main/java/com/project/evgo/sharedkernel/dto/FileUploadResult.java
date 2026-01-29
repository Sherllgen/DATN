package com.project.evgo.sharedkernel.dto;

import lombok.Builder;

/**
 * Generic response for file upload operations.
 * Used by FileStorageService implementations.
 * Part of sharedkernel - accessible by all modules.
 */
@Builder
public record FileUploadResult(
	String fileUrl,
	String publicId) {
}
