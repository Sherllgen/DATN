package com.project.evgo.user.request;

import com.project.evgo.sharedkernel.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/**
 * Request DTO for filtering account list.
 * Public API - accessible by other modules.
 */
@Builder
@Schema(description = "Account filter request for pagination and filtering")
public record AccountFilterRequest(
	@Schema(description = "Page number (0-indexed)", example = "0") 
	Integer page,

	@Schema(description = "Page size", example = "10") 
	Integer size,

	@Schema(description = "Sort field", example = "createdAt") 
	String sortBy,

	@Schema(description = "Sort direction: ASC or DESC", example = "DESC") 
	String sortDir,

	@Schema(description = "Filter by user status") 
	UserStatus status,

	@Schema(description = "Filter by role name", example = "ROLE_USER") 
	String role,

	@Schema(description = "Search by email or fullName") 
	String search) {
}
