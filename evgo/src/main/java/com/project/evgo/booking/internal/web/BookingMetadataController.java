package com.project.evgo.booking.internal.web;

import com.project.evgo.booking.BookingMetadataService;
import com.project.evgo.booking.response.AvailableSlotResponse;
import com.project.evgo.booking.response.CalendarStatusResponse;
import com.project.evgo.booking.response.DurationConfigResponse;
import com.project.evgo.sharedkernel.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Booking Metadata", description = "Metadata for booking EV charging slots")
public class BookingMetadataController {

    private final BookingMetadataService bookingMetadataService;

    @GetMapping("/config/durations")
    @Operation(summary = "Get list of charging durations")
    public ResponseEntity<ApiResponse<DurationConfigResponse>> getDurations() {
        DurationConfigResponse result = bookingMetadataService.getDurations();
        return ResponseEntity.ok(ApiResponse.<DurationConfigResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Success")
                .data(result)
                .build());
    }

    @GetMapping("/bookings/calendar-status")
    @Operation(summary = "Get days in a month with availability status")
    public ResponseEntity<ApiResponse<List<CalendarStatusResponse>>> getCalendarStatus(
            @RequestParam("stationId") Long stationId,
            @RequestParam("month") @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        
        List<CalendarStatusResponse> result = bookingMetadataService.getCalendarStatus(stationId, month);
        return ResponseEntity.ok(ApiResponse.<List<CalendarStatusResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Success")
                .data(result)
                .build());
    }

    @GetMapping("/bookings/available-slots")
    @Operation(summary = "Calculates and returns available time slots")
    public ResponseEntity<ApiResponse<List<AvailableSlotResponse>>> getAvailableSlots(
            @RequestParam("stationId") Long stationId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam("duration") Double durationHour) {
            
        List<AvailableSlotResponse> result = bookingMetadataService.getAvailableSlots(stationId, date, durationHour);
        return ResponseEntity.ok(ApiResponse.<List<AvailableSlotResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Success")
                .data(result)
                .build());
    }
}
