package com.project.evgo.charger.internal.web;

import com.project.evgo.charger.ChargerService;
import com.project.evgo.charger.response.ChargerResponse;
import com.project.evgo.charger.response.SlotResponse;
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
 * REST controller for charger and slot management.
 */
@RestController
@RequestMapping("/api/v1/chargers")
@RequiredArgsConstructor
@Tag(name = "Chargers", description = "Charger & Slot management APIs")
public class ChargerController {

    private final ChargerService chargerService;

    @GetMapping
    @Operation(summary = "Get chargers by station ID")
    public ResponseEntity<ApiResponse<List<ChargerResponse>>> getByStationId(
            @RequestParam Long stationId) {
        var result = chargerService.findByStationId(stationId);
        return ResponseEntity.ok(ApiResponse.<List<ChargerResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Success")
                .data(result)
                .build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get charger by ID")
    public ResponseEntity<ApiResponse<ChargerResponse>> getById(@PathVariable Long id) {
        var result = chargerService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        return ResponseEntity.ok(ApiResponse.<ChargerResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Success")
                .data(result)
                .build());
    }

    @GetMapping("/{id}/slots")
    @Operation(summary = "Get slots by charger ID")
    public ResponseEntity<ApiResponse<List<SlotResponse>>> getSlotsByChargerId(
            @PathVariable Long id) {
        var result = chargerService.findSlotsByChargerId(id);
        return ResponseEntity.ok(ApiResponse.<List<SlotResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Success")
                .data(result)
                .build());
    }

    @GetMapping("/slots/{slotId}")
    @Operation(summary = "Get slot by ID")
    public ResponseEntity<ApiResponse<SlotResponse>> getSlotById(@PathVariable Long slotId) {
        var result = chargerService.findSlotById(slotId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        return ResponseEntity.ok(ApiResponse.<SlotResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Success")
                .data(result)
                .build());
    }
}
