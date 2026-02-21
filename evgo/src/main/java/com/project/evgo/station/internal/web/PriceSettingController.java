package com.project.evgo.station.internal.web;

import com.project.evgo.sharedkernel.dto.ApiResponse;
import com.project.evgo.station.PriceSettingService;
import com.project.evgo.station.request.CreatePriceSettingRequest;
import com.project.evgo.station.response.PriceSettingResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST Controller for station pricing management.
 */
@RestController
@RequestMapping("/api/v1/stations/{stationId}/pricing")
@RequiredArgsConstructor
@Tag(name = "Station Pricing", description = "Station pricing management APIs")
public class PriceSettingController {

    private final PriceSettingService priceSettingService;

    @PostMapping
    @PreAuthorize("hasRole('STATION_OWNER')")
    @Operation(summary = "Create pricing", description = "Create a new pricing version for a station (owner only)")
    public ResponseEntity<ApiResponse<PriceSettingResponse>> createPriceSetting(
            @PathVariable Long stationId,
            @Valid @RequestBody CreatePriceSettingRequest request) {
        PriceSettingResponse result = priceSettingService.createPriceSetting(stationId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<PriceSettingResponse>builder()
                        .status(HttpStatus.CREATED.value())
                        .message("Price setting created successfully")
                        .data(result)
                        .build());
    }

    @GetMapping
    @Operation(summary = "Get active pricing", description = "Get the current active pricing for a station (public)")
    public ResponseEntity<ApiResponse<PriceSettingResponse>> getActivePriceSetting(
            @PathVariable Long stationId) {
        PriceSettingResponse result = priceSettingService.getActivePriceSetting(stationId);
        return ResponseEntity.ok(ApiResponse.<PriceSettingResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Success")
                .data(result)
                .build());
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('STATION_OWNER')")
    @Operation(summary = "Get pricing history", description = "Get all pricing versions for a station (owner only)")
    public ResponseEntity<ApiResponse<List<PriceSettingResponse>>> getPricingHistory(
            @PathVariable Long stationId) {
        List<PriceSettingResponse> result = priceSettingService.getPricingHistory(stationId);
        return ResponseEntity.ok(ApiResponse.<List<PriceSettingResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Success")
                .data(result)
                .build());
    }

    @GetMapping("/calculate-idle-fee")
    @Operation(summary = "Calculate idle fee", description = "Calculate the overstay idle fee based on minutes (public)")
    public ResponseEntity<ApiResponse<BigDecimal>> calculateIdleFee(
            @PathVariable Long stationId,
            @RequestParam @Min(0) int overstayMinutes) {
        BigDecimal fee = priceSettingService.calculateIdleFee(stationId, overstayMinutes);
        return ResponseEntity.ok(ApiResponse.<BigDecimal>builder()
                .status(HttpStatus.OK.value())
                .message("Success")
                .data(fee)
                .build());
    }
}
