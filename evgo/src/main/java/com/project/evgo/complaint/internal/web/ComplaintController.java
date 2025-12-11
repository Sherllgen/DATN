package com.project.evgo.complaint.internal.web;

import com.project.evgo.complaint.ComplaintService;
import com.project.evgo.complaint.response.ComplaintResponse;
import com.project.evgo.sharedkernel.dto.ApiResponse;
import com.project.evgo.sharedkernel.enums.ErrorCode;
import com.project.evgo.sharedkernel.exceptions.AppException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for complaint management.
 */
@RestController
@RequestMapping("/api/v1/complaints")
@RequiredArgsConstructor
@Tag(name = "Complaints", description = "Complaint management APIs")
public class ComplaintController {

    private final ComplaintService complaintService;

    @GetMapping("/{id}")
    @Operation(summary = "Get complaint by ID")
    public ResponseEntity<ApiResponse<ComplaintResponse>> getById(@PathVariable Long id) {
        var result = complaintService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        return ResponseEntity.ok(ApiResponse.<ComplaintResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Success")
                .data(result)
                .build());
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get complaints by user ID")
    public ResponseEntity<ApiResponse<List<ComplaintResponse>>> getByUserId(
            @PathVariable Long userId) {
        var result = complaintService.findByUserId(userId);
        return ResponseEntity.ok(ApiResponse.<List<ComplaintResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Success")
                .data(result)
                .build());
    }

    @GetMapping("/station/{stationId}")
    @Operation(summary = "Get complaints by station ID")
    public ResponseEntity<ApiResponse<List<ComplaintResponse>>> getByStationId(
            @PathVariable Long stationId) {
        var result = complaintService.findByStationId(stationId);
        return ResponseEntity.ok(ApiResponse.<List<ComplaintResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Success")
                .data(result)
                .build());
    }
}
