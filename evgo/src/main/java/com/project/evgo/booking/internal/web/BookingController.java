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
import jakarta.validation.Valid;

import java.util.List;

import com.project.evgo.booking.request.CheckAvailabilityRequest;
import com.project.evgo.booking.request.CreateBookingRequest;
import com.project.evgo.sharedkernel.dto.PageResponse;
import com.project.evgo.user.security.SecurityUtil;

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
        BookingResponse result = bookingService.findById(id)
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
        List<BookingResponse> result = bookingService.findByUserId(userId);
        return ResponseEntity.ok(ApiResponse.<List<BookingResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Success")
                .data(result)
                .build());
    }

    @GetMapping("/my")
    @Operation(summary = "Get my bookings")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getMyBookings() {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        List<BookingResponse> result = bookingService.findByUserId(currentUserId);
        return ResponseEntity.ok(ApiResponse.<List<BookingResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Success")
                .data(result)
                .build());
    }

    @GetMapping("/station/{stationId}/port/{portNumber}")
    @Operation(summary = "Get bookings by station ID and port number")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getByStationIdAndPortNumber(
            @PathVariable Long stationId,
            @PathVariable Integer portNumber) {
        List<BookingResponse> result = bookingService.findByStationIdAndPortNumber(stationId, portNumber);
        return ResponseEntity.ok(ApiResponse.<List<BookingResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Success")
                .data(result)
                .build());
    }

    @PostMapping("/check-availability")
    @Operation(summary = "Check availability and create temporary lock")
    public ResponseEntity<ApiResponse<Void>> checkAvailability(
            @Valid @RequestBody CheckAvailabilityRequest request) {
        bookingService.checkAvailability(request);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Available")
                .data(null)
                .build());
    }

    @PostMapping
    @Operation(summary = "Create a PENDING booking")
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @Valid @RequestBody CreateBookingRequest request) {
        BookingResponse result = bookingService.createBooking(request);
        return ResponseEntity.ok(ApiResponse.<BookingResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Success")
                .data(result)
                .build());
    }

    @GetMapping
    @Operation(summary = "Get bookings by status with pagination")
    public ResponseEntity<ApiResponse<PageResponse<BookingResponse>>> getBookingsByStatus(
            @RequestParam String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<BookingResponse> result = bookingService.getBookingsByStatus(status, page, size);
        return ResponseEntity.ok(ApiResponse.<PageResponse<BookingResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Success")
                .data(result)
                .build());
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel a booking")
    public ResponseEntity<ApiResponse<Void>> cancelBooking(@PathVariable Long id) {
        bookingService.cancelBooking(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Booking cancelled successfully")
                .data(null)
                .build());
    }
}
