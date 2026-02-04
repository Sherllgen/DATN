package com.project.evgo.charger.internal.web;

import com.project.evgo.charger.ChargerService;
import com.project.evgo.charger.request.CreateChargerRequest;
import com.project.evgo.charger.request.CreatePortRequest;
import com.project.evgo.charger.request.UpdateChargerRequest;
import com.project.evgo.charger.request.UpdatePortRequest;
import com.project.evgo.charger.response.ChargerResponse;
import com.project.evgo.charger.response.PortResponse;
import com.project.evgo.sharedkernel.dto.ApiResponse;
import com.project.evgo.sharedkernel.enums.ErrorCode;
import com.project.evgo.sharedkernel.exceptions.AppException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

        // ==================== CHARGER ENDPOINTS ====================

        @GetMapping
        @Operation(summary = "Get chargers by station ID", description = "Get all chargers for a station (public)")
        public ResponseEntity<ApiResponse<List<ChargerResponse>>> getByStationId(
                        @RequestParam Long stationId) {
                List<ChargerResponse> result = chargerService.findByStationId(stationId);
                return ResponseEntity.ok(ApiResponse.<List<ChargerResponse>>builder()
                                .status(HttpStatus.OK.value())
                                .message("Success")
                                .data(result)
                                .build());
        }

        @GetMapping("/{id}")
        @Operation(summary = "Get charger by ID", description = "Get charger details by ID (public)")
        public ResponseEntity<ApiResponse<ChargerResponse>> getById(@PathVariable Long id) {
                ChargerResponse result = chargerService.findById(id)
                                .orElseThrow(() -> new AppException(ErrorCode.CHARGER_NOT_FOUND));
                return ResponseEntity.ok(ApiResponse.<ChargerResponse>builder()
                                .status(HttpStatus.OK.value())
                                .message("Success")
                                .data(result)
                                .build());
        }

        @PostMapping
        @PreAuthorize("hasRole('STATION_OWNER')")
        @Operation(summary = "Create charger", description = "Create a new charger (station owner only)")
        public ResponseEntity<ApiResponse<ChargerResponse>> create(
                        @Valid @RequestBody CreateChargerRequest request) {
                ChargerResponse result = chargerService.createCharger(request);
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.<ChargerResponse>builder()
                                                .status(HttpStatus.CREATED.value())
                                                .message("Charger created successfully")
                                                .data(result)
                                                .build());
        }

        @PutMapping("/{id}")
        @PreAuthorize("hasRole('STATION_OWNER')")
        @Operation(summary = "Update charger", description = "Update an existing charger (owner only)")
        public ResponseEntity<ApiResponse<ChargerResponse>> update(
                        @PathVariable Long id,
                        @Valid @RequestBody UpdateChargerRequest request) {
                ChargerResponse result = chargerService.updateCharger(
                                id, request.name(), request.maxPower(), request.connectorType());
                return ResponseEntity.ok(ApiResponse.<ChargerResponse>builder()
                                .status(HttpStatus.OK.value())
                                .message("Charger updated successfully")
                                .data(result)
                                .build());
        }

        @DeleteMapping("/{id}")
        @PreAuthorize("hasRole('STATION_OWNER')")
        @Operation(summary = "Delete charger", description = "Delete a charger (owner only)")
        public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
                chargerService.deleteCharger(id);
                return ResponseEntity.ok(ApiResponse.<Void>builder()
                                .status(HttpStatus.OK.value())
                                .message("Charger deleted successfully")
                                .build());
        }

        // ==================== PORT ENDPOINTS ====================

        @GetMapping("/{id}/ports")
        @Operation(summary = "Get ports by charger ID", description = "Get all ports for a charger (public)")
        public ResponseEntity<ApiResponse<List<PortResponse>>> getPortsByChargerId(
                        @PathVariable Long id) {
                List<PortResponse> result = chargerService.findPortsByChargerId(id);
                return ResponseEntity.ok(ApiResponse.<List<PortResponse>>builder()
                                .status(HttpStatus.OK.value())
                                .message("Success")
                                .data(result)
                                .build());
        }

        @GetMapping("/ports/{portId}")
        @Operation(summary = "Get port by ID", description = "Get port details by ID (public)")
        public ResponseEntity<ApiResponse<PortResponse>> getPortById(@PathVariable Long portId) {
                PortResponse result = chargerService.findPortById(portId)
                                .orElseThrow(() -> new AppException(ErrorCode.PORT_NOT_FOUND));
                return ResponseEntity.ok(ApiResponse.<PortResponse>builder()
                                .status(HttpStatus.OK.value())
                                .message("Success")
                                .data(result)
                                .build());
        }

        @PostMapping("/{chargerId}/ports")
        @PreAuthorize("hasRole('STATION_OWNER')")
        @Operation(summary = "Create port", description = "Create a new port for a charger (owner only)")
        public ResponseEntity<ApiResponse<PortResponse>> createPort(
                        @PathVariable Long chargerId,
                        @Valid @RequestBody CreatePortRequest request) {
                // Override chargerId from path
                CreatePortRequest portRequest = new CreatePortRequest();
                portRequest.setPortNumber(request.getPortNumber());
                portRequest.setChargerId(chargerId);

                PortResponse result = chargerService.createPort(portRequest);
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.<PortResponse>builder()
                                                .status(HttpStatus.CREATED.value())
                                                .message("Port created successfully")
                                                .data(result)
                                                .build());
        }

        @PatchMapping("/ports/{portId}/status")
        @PreAuthorize("hasRole('STATION_OWNER')")
        @Operation(summary = "Update port status", description = "Update port status (owner only)")
        public ResponseEntity<ApiResponse<PortResponse>> updatePortStatus(
                        @PathVariable Long portId,
                        @Valid @RequestBody UpdatePortRequest request) {
                PortResponse result = chargerService.updatePortStatus(portId, request.status());
                return ResponseEntity.ok(ApiResponse.<PortResponse>builder()
                                .status(HttpStatus.OK.value())
                                .message("Port status updated successfully")
                                .data(result)
                                .build());
        }

        @DeleteMapping("/ports/{portId}")
        @PreAuthorize("hasRole('STATION_OWNER')")
        @Operation(summary = "Delete port", description = "Delete a port (owner only)")
        public ResponseEntity<ApiResponse<Void>> deletePort(@PathVariable Long portId) {
                chargerService.deletePort(portId);
                return ResponseEntity.ok(ApiResponse.<Void>builder()
                                .status(HttpStatus.OK.value())
                                .message("Port deleted successfully")
                                .build());
        }
}
