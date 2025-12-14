package com.project.evgo.charger.internal.web;

import com.project.evgo.charger.ChargerService;
import com.project.evgo.charger.response.ChargerResponse;
import com.project.evgo.charger.response.PortResponse;
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
 * REST controller for charger and port management.
 */
@RestController
@RequestMapping("/api/v1/chargers")
@RequiredArgsConstructor
@Tag(name = "Chargers", description = "Charger & Port management APIs")
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

    @GetMapping("/{id}/ports")
    @Operation(summary = "Get ports by charger ID")
    public ResponseEntity<ApiResponse<List<PortResponse>>> getPortsByChargerId(
            @PathVariable Long id) {
        var result = chargerService.findPortsByChargerId(id);
        return ResponseEntity.ok(ApiResponse.<List<PortResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Success")
                .data(result)
                .build());
    }

    @GetMapping("/ports/{portId}")
    @Operation(summary = "Get port by ID")
    public ResponseEntity<ApiResponse<PortResponse>> getPortById(@PathVariable Long portId) {
        var result = chargerService.findPortById(portId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        return ResponseEntity.ok(ApiResponse.<PortResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Success")
                .data(result)
                .build());
    }
}
