package com.project.evgo.navigation.internal.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.evgo.navigation.DirectionService;
import com.project.evgo.navigation.response.RouteResponse;
import com.project.evgo.sharedkernel.dto.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/stations/directions")
@RequiredArgsConstructor
@Tag(name = "Navigation", description = "Navigation API for routing and directions")
public class DirectionController {

    private final DirectionService directionService;

    @Operation(summary = "Get route directions", description = "Get encoded polyline, distance, and duration between two coordinates")
    @GetMapping
    public ResponseEntity<ApiResponse<RouteResponse>> getRoute(
            @Parameter(description = "Origin Latitude", example = "10.762622") @RequestParam Double originLat,
            @Parameter(description = "Origin Longitude", example = "106.660172") @RequestParam Double originLng,
            @Parameter(description = "Destination Latitude", example = "10.8231") @RequestParam Double destLat,
            @Parameter(description = "Destination Longitude", example = "106.6297") @RequestParam Double destLng) {

        RouteResponse route = directionService.getRoute(originLat, originLng, destLat, destLng);

        return ResponseEntity.ok(ApiResponse.<RouteResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Success")
                .data(route)
                .build());
    }
}
