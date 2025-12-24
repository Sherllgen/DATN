package com.project.evgo.user.internal.web;

import com.project.evgo.sharedkernel.dto.ApiResponse;
import com.project.evgo.user.VehicleService;
import com.project.evgo.user.request.CreateVehicleRequest;
import com.project.evgo.user.request.UpdateVehicleRequest;
import com.project.evgo.user.response.VehicleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing user vehicles.
 */
@RestController
@RequestMapping("/api/v1/users/me/vehicles")
@RequiredArgsConstructor
@Tag(name = "User Vehicle Management", description = "APIs for managing user's vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    @PostMapping
    @Operation(summary = "Add a new vehicle")
    public ResponseEntity<ApiResponse<VehicleResponse>> addVehicle(@Valid @RequestBody CreateVehicleRequest request) {
        VehicleResponse vehicle = vehicleService.addVehicle(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<VehicleResponse>builder()
                        .status(HttpStatus.CREATED.value())
                        .message("Vehicle created successfully")
                        .data(vehicle)
                        .build());
    }

    @GetMapping
    @Operation(summary = "Get my vehicles")
    public ResponseEntity<ApiResponse<List<VehicleResponse>>> getMyVehicles() {
        List<VehicleResponse> vehicles = vehicleService.getMyVehicles();
        return ResponseEntity.ok(ApiResponse.<List<VehicleResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Success")
                .data(vehicles)
                .build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get vehicle details by ID")
    public ResponseEntity<ApiResponse<VehicleResponse>> getVehicleById(@PathVariable Long id) {
        VehicleResponse vehicle = vehicleService.getVehicleById(id);
        return ResponseEntity.ok(ApiResponse.<VehicleResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Success")
                .data(vehicle)
                .build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update vehicle details")
    public ResponseEntity<ApiResponse<VehicleResponse>> updateVehicle(
            @PathVariable Long id,
            @Valid @RequestBody UpdateVehicleRequest request) {
        VehicleResponse vehicle = vehicleService.updateVehicle(id, request);
        return ResponseEntity.ok(ApiResponse.<VehicleResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Vehicle updated successfully")
                .data(vehicle)
                .build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a vehicle")
    public ResponseEntity<ApiResponse<Void>> deleteVehicle(@PathVariable Long id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Vehicle deleted successfully")
                .build());
    }

    @PutMapping("/{id}/in-use")
    @Operation(summary = "Set vehicle as in use", description = "Marks this vehicle as the currently active vehicle for station search")
    public ResponseEntity<ApiResponse<VehicleResponse>> setVehicleInUse(@PathVariable Long id) {
        VehicleResponse vehicle = vehicleService.setVehicleInUse(id);
        return ResponseEntity.ok(ApiResponse.<VehicleResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Vehicle set as in use")
                .data(vehicle)
                .build());
    }

    @GetMapping("/in-use")
    @Operation(summary = "Get current in-use vehicle", description = "Returns the vehicle currently marked as in use for station search")
    public ResponseEntity<ApiResponse<VehicleResponse>> getInUseVehicle() {
        VehicleResponse vehicle = vehicleService.getInUseVehicle();
        return ResponseEntity.ok(ApiResponse.<VehicleResponse>builder()
                .status(HttpStatus.OK.value())
                .message(vehicle != null ? "Success" : "No vehicle in use")
                .data(vehicle)
                .build());
    }
}
