package com.project.evgo.charging.internal.web;

import com.project.evgo.charging.ChargingService;
import com.project.evgo.charging.response.ChargingSessionResponse;
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
        var result = chargingService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        return ResponseEntity.ok(ApiResponse.<ChargingSessionResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Success")
                .data(result)
                .build());
    }

    @GetMapping("/booking/{bookingId}")
    @Operation(summary = "Get charging session by booking ID")
    public ResponseEntity<ApiResponse<ChargingSessionResponse>> getByBookingId(
            @PathVariable Long bookingId) {
        var result = chargingService.findByBookingId(bookingId)
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
        var result = chargingService.findByUserId(userId);
        return ResponseEntity.ok(ApiResponse.<List<ChargingSessionResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Success")
                .data(result)
                .build());
    }
}
