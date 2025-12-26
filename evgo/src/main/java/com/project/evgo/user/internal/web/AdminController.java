package com.project.evgo.user.internal.web;

import com.project.evgo.sharedkernel.dto.ApiResponse;
import com.project.evgo.sharedkernel.dto.PageResponse;
import com.project.evgo.sharedkernel.enums.UserStatus;
import com.project.evgo.user.AccountManagementService;
import com.project.evgo.user.AdminReviewService;
import com.project.evgo.user.request.AccountFilterRequest;
import com.project.evgo.user.request.RejectionRequest;
import com.project.evgo.user.response.AdminAccountResponse;
import com.project.evgo.user.response.PendingRegistrationResponse;
import com.project.evgo.user.response.RegistrationDetailResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Account Administrator", description = "APIs for administrators to manage accounts")
public class AdminController {

	private final AdminReviewService adminReviewService;
	private final AccountManagementService accountManagementService;

	// ==================== Account Management APIs ====================

	@GetMapping("/accounts")
	@Operation(summary = "List all accounts", description = "Retrieve all user accounts with pagination and filtering (Super Admin only)")
	public ResponseEntity<ApiResponse<PageResponse<AdminAccountResponse>>> getAllAccounts(
				@RequestParam(defaultValue = "0") Integer page,
				@RequestParam(defaultValue = "10") Integer size,
				@RequestParam(defaultValue = "createdAt") String sortBy,
				@RequestParam(defaultValue = "DESC") String sortDir,
				@RequestParam(required = false) UserStatus status,
				@RequestParam(required = false) String role,
				@RequestParam(required = false) String search) {

		AccountFilterRequest request = AccountFilterRequest.builder()
						.page(page)
						.size(size)
						.sortBy(sortBy)
						.sortDir(sortDir)
						.status(status)
						.role(role)
						.search(search)
						.build();

		PageResponse<AdminAccountResponse> accounts = accountManagementService.findAllAccounts(request);
		return ResponseEntity.ok(new ApiResponse<>(
						HttpStatus.OK.value(),
						"Accounts retrieved successfully",
						accounts));
	}

	@GetMapping("/accounts/{userId}")
	@Operation(summary = "Get account details", description = "Get detailed information about a specific account (Super Admin only)")
	public ResponseEntity<ApiResponse<AdminAccountResponse>> getAccountById(@PathVariable Long userId) {
		AdminAccountResponse account = accountManagementService.getAccountById(userId);
		return ResponseEntity.ok(new ApiResponse<>(
						HttpStatus.OK.value(),
						"Account retrieved successfully",
						account));
	}

	@PostMapping("/accounts/{userId}/lock")
	@Operation(summary = "Lock account", description = "Lock a user account - sets status to BLOCKED (Super Admin only)")
	public ResponseEntity<ApiResponse<Void>> lockAccount(@PathVariable Long userId) {
		accountManagementService.lockAccount(userId);
		return ResponseEntity.ok(new ApiResponse<>(
						HttpStatus.OK.value(),
						"Account locked successfully",
						null));
	}

	@PostMapping("/accounts/{userId}/unlock")
	@Operation(summary = "Unlock account", description = "Unlock a user account - sets status to ACTIVE (Super Admin only)")
	public ResponseEntity<ApiResponse<Void>> unlockAccount(@PathVariable Long userId) {
		accountManagementService.unlockAccount(userId);
		return ResponseEntity.ok(new ApiResponse<>(
						HttpStatus.OK.value(),
						"Account unlocked successfully",
						null));
	}

	@DeleteMapping("/accounts/{userId}")
	@Operation(summary = "Delete account", description = "Soft delete a user account - sets status to DELETED (Super Admin only)")
	public ResponseEntity<ApiResponse<Void>> deleteAccount(@PathVariable Long userId) {
		accountManagementService.deleteAccount(userId);
		return ResponseEntity.ok(new ApiResponse<>(
						HttpStatus.OK.value(),
						"Account deleted successfully",
						null));
	}

	// ==================== Station Owner Registration APIs ====================

	@GetMapping("/station-owner/pending")
	@Operation(summary = "Get pending registrations", description = "Retrieve all pending station owner registrations with pagination (Admin only)")
	public ResponseEntity<ApiResponse<PageResponse<PendingRegistrationResponse>>> getPendingRegistrations(
					@RequestParam(defaultValue = "0") int page,
					@RequestParam(defaultValue = "10") int size,
					@RequestParam(defaultValue = "ASC") String sortDir,
					@RequestParam(defaultValue = "submittedAt") String sortBy) {

		Pageable pageable = PageRequest.of(
						page,
						size,
						Sort.Direction.fromString(sortDir),
						sortBy);

		PageResponse<PendingRegistrationResponse> registrations = adminReviewService
						.getPendingRegistrations(pageable);
		return ResponseEntity.ok(new ApiResponse<>(
						HttpStatus.OK.value(),
						"Pending registrations retrieved successfully",
						registrations));
	}

	@GetMapping("/station-owner/{profileId}")
	@Operation(summary = "Get registration details", description = "Get detailed information about a registration (Admin only)")
	public ResponseEntity<ApiResponse<RegistrationDetailResponse>> getRegistrationDetail(
					@PathVariable Long profileId) {
		RegistrationDetailResponse response = adminReviewService.getRegistrationDetail(profileId);
		return ResponseEntity.ok(new ApiResponse<>(
						HttpStatus.OK.value(),
						"Registration details retrieved successfully",
						response));
	}

	@PostMapping("/station-owner/{profileId}/approve")
	@Operation(summary = "Approve registration", description = "Approve a pending station owner registration (Admin only)")
	public ResponseEntity<ApiResponse<Void>> approveRegistration(@PathVariable Long profileId) {
		adminReviewService.approveRegistration(profileId);
		return ResponseEntity.ok(new ApiResponse<>(
						HttpStatus.OK.value(),
						"Registration approved successfully",
						null));
	}

	@PostMapping("/station-owner/{profileId}/reject")
	@Operation(summary = "Reject registration", description = "Reject a pending station owner registration (Admin only)")
	public ResponseEntity<ApiResponse<Void>> rejectRegistration(
					@PathVariable Long profileId,
					@RequestBody RejectionRequest request) {
		adminReviewService.rejectRegistration(profileId, request);
		return ResponseEntity.ok(new ApiResponse<>(
						HttpStatus.OK.value(),
						"Registration rejected successfully",
						null));
	}
}
