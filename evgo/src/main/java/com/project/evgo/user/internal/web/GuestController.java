package com.project.evgo.user.internal.web;

import com.project.evgo.sharedkernel.dto.ApiResponse;
import com.project.evgo.user.StationOwnerService;
import com.project.evgo.user.request.TrackingRequest;
import com.project.evgo.user.response.TrackingResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/guests")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management APIs")
public class GuestController {

    private final StationOwnerService stationOwnerService;

    @GetMapping("/track")
    @Operation(summary = "Track Station Owner Registration Status", description = "Allows guest users to track the status of their station owner registration using email or registration code.")
    public ResponseEntity<ApiResponse<TrackingResponse>> trackGuestUserRegistrationStatus(
            @RequestBody @Valid TrackingRequest request) {
        TrackingResponse response = stationOwnerService.getStatus(request);
        return ResponseEntity.ok(new ApiResponse<>(
                200,
                "Tracking status retrieved successfully",
                response));
    }

}
