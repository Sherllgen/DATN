package com.project.evgo.user.internal.web;

import com.project.evgo.sharedkernel.dto.ApiResponse;
import com.project.evgo.sharedkernel.dto.PageResponse;
import com.project.evgo.user.AdminReviewService;
import com.project.evgo.user.request.PaginationRequest;
import com.project.evgo.user.request.RejectionRequest;
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
@Tag(name = "Account Administrator", description = "APIs for administrators to manage others' accounts")
public class AdminController {
    private final AdminReviewService adminReviewService;

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
                sortBy
        );

        PageResponse<PendingRegistrationResponse> registrations = adminReviewService.getPendingRegistrations(pageable);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "Pending registrations retrieved successfully",
                        registrations));
    }


    @GetMapping("/station-owner/{profileId}")
    @Operation(summary = "Get registration details", description = "Get detailed information about a registration (Admin only)")
    public ResponseEntity<ApiResponse<RegistrationDetailResponse>> getRegistrationDetail(@PathVariable Long profileId) {
        RegistrationDetailResponse response = adminReviewService.getRegistrationDetail(profileId);
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK.value(),
                "Registration details retrieved successfully",
                response));
    }

    @PostMapping("/station-owner/{profileId}/approve")
    @Operation(summary = "Approve registration", description = "Approve a pending station owner registration (Admin only)")
    public ResponseEntity<ApiResponse<Void>> approveRegistration(
            @PathVariable Long profileId) {
        adminReviewService.approveRegistration(profileId);
        return ResponseEntity.ok(
                new ApiResponse<>(
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
        return ResponseEntity.ok(
                new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "Registration rejected successfully",
                        null));
    }
}
