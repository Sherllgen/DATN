package com.project.evgo.station.internal.web;

import com.project.evgo.sharedkernel.dto.ApiResponse;
import com.project.evgo.sharedkernel.dto.PageResponse;
import com.project.evgo.sharedkernel.enums.StationStatus;
import com.project.evgo.station.StationAdminService;
import com.project.evgo.station.request.RejectStationRequest;
import com.project.evgo.station.request.SuspendStationRequest;
import com.project.evgo.station.response.StationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for admin station review operations.
 */
@RestController
@RequestMapping("/api/v1/admin/stations")
@RequiredArgsConstructor
@Tag(name = "Station Administrator", description = "APIs for administrators to manage stations")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class StationAdminController {

    private final StationAdminService stationAdminService;

    @GetMapping
    @Operation(summary = "Get all stations", description = "Get all stations with optional status filter (Admin only)")
    public ResponseEntity<ApiResponse<PageResponse<StationResponse>>> getAllStations(
            @RequestParam(required = false) StationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        PageResponse<StationResponse> stations = stationAdminService.getAllStations(status, pageable);

        return ResponseEntity.ok(ApiResponse.<PageResponse<StationResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Stations retrieved successfully")
                .data(stations)
                .build());
    }

    @GetMapping("/{stationId}")
    @Operation(summary = "Get station details", description = "Get detailed information about a station (Admin only)")
    public ResponseEntity<ApiResponse<StationResponse>> getStationById(
            @PathVariable Long stationId) {

        StationResponse station = stationAdminService.getStationById(stationId);

        return ResponseEntity.ok(ApiResponse.<StationResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Station retrieved successfully")
                .data(station)
                .build());
    }

    @PostMapping("/{stationId}/approve")
    @Operation(summary = "Approve station", description = "Approve a pending station (Admin only)")
    public ResponseEntity<ApiResponse<Void>> approveStation(
            @PathVariable Long stationId) {

        stationAdminService.approveStation(stationId);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Station approved successfully")
                .build());
    }

    @PostMapping("/{stationId}/reject")
    @Operation(summary = "Reject station", description = "Reject a pending station (Admin only)")
    public ResponseEntity<ApiResponse<Void>> rejectStation(
            @PathVariable Long stationId,
            @RequestBody(required = false) RejectStationRequest request) {

        String reason = request != null ? request.reason() : "No reason provided";
        stationAdminService.rejectStation(stationId, reason);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Station rejected successfully")
                .build());
    }

    @PostMapping("/{stationId}/suspend")
    @Operation(summary = "Suspend station", description = "Suspend an active station (Admin only)")
    public ResponseEntity<ApiResponse<Void>> suspendStation(
            @PathVariable Long stationId,
            @RequestBody(required = false) SuspendStationRequest request) {

        String reason = request != null ? request.reason() : "No reason provided";
        stationAdminService.suspendStation(stationId, reason);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Station suspended successfully")
                .build());
    }

    @PostMapping("/{stationId}/unsuspend")
    @Operation(summary = "Unsuspend station", description = "Unsuspend a suspended station (Admin only)")
    public ResponseEntity<ApiResponse<Void>> unsuspendStation(
            @PathVariable Long stationId) {

        stationAdminService.unsuspendStation(stationId);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Station unsuspended successfully")
                .build());
    }
}
