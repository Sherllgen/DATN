package com.project.evgo.charging.internal.web;

import com.project.evgo.charging.ChargingService;
import com.project.evgo.charging.request.StartChargingRequest;
import com.project.evgo.charging.request.StopChargingRequest;
import com.project.evgo.charging.response.ChargingSessionResponse;
import com.project.evgo.sharedkernel.dto.ApiResponse;
import com.project.evgo.sharedkernel.enums.ErrorCode;
import com.project.evgo.sharedkernel.exceptions.AppException;
import com.project.evgo.user.security.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for charging session management.
 */
@RestController
@RequestMapping("/api/v1/charging")
@RequiredArgsConstructor
@Tag(name = "Charging", description = "Charging session management APIs")
public class ChargingController {

    private final ChargingService chargingService;

    @GetMapping("/{id}")
    @Operation(summary = "Get charging session by ID")
    public ResponseEntity<ApiResponse<ChargingSessionResponse>> getById(@PathVariable Long id) {
        ChargingSessionResponse result = chargingService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        return ResponseEntity.ok(ApiResponse.<ChargingSessionResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Success")
                .data(result)
                .build());
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get charging sessions by user ID")
    public ResponseEntity<ApiResponse<List<ChargingSessionResponse>>> getByUserId(
            @PathVariable Long userId) {
        List<ChargingSessionResponse> result = chargingService.findByUserId(userId);
        return ResponseEntity.ok(ApiResponse.<List<ChargingSessionResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Success")
                .data(result)
                .build());
    }

    @PostMapping("/start")
    @Operation(summary = "Start a charging session")
    public ResponseEntity<ApiResponse<ChargingSessionResponse>> startCharging(
            @Valid @RequestBody StartChargingRequest request) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        ChargingSessionResponse result = chargingService.startCharging(request, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<ChargingSessionResponse>builder()
                        .status(HttpStatus.CREATED.value())
                        .message("Charging session initiated successfully")
                        .data(result)
                        .build());
    }

    @PostMapping("/stop")
    @Operation(summary = "Stop a charging session")
    public ResponseEntity<ApiResponse<Void>> stopCharging(
            @Valid @RequestBody StopChargingRequest request) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        chargingService.stopCharging(request, currentUserId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Stop charging command sent")
                .data(null)
                .build());
    }
}
