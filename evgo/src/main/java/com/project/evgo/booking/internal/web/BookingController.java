package com.project.evgo.booking.internal.web;

import com.project.evgo.booking.BookingService;
import com.project.evgo.booking.response.BookingResponse;
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
 * REST controller for booking management.
 */
@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Booking management APIs")
public class BookingController {

    private final BookingService bookingService;

    @GetMapping("/{id}")
    @Operation(summary = "Get booking by ID")
    public ResponseEntity<ApiResponse<BookingResponse>> getById(@PathVariable Long id) {
        var result = bookingService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        return ResponseEntity.ok(ApiResponse.<BookingResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Success")
                .data(result)
                .build());
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get bookings by user ID")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getByUserId(
            @PathVariable Long userId) {
        var result = bookingService.findByUserId(userId);
        return ResponseEntity.ok(ApiResponse.<List<BookingResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Success")
                .data(result)
                .build());
    }

    @GetMapping("/slot/{slotId}")
    @Operation(summary = "Get bookings by slot ID")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getBySlotId(
            @PathVariable Long slotId) {
        var result = bookingService.findBySlotId(slotId);
        return ResponseEntity.ok(ApiResponse.<List<BookingResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Success")
                .data(result)
                .build());
    }
}
