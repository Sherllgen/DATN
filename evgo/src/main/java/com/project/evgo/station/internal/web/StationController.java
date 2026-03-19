package com.project.evgo.station.internal.web;

import com.project.evgo.sharedkernel.dto.ApiResponse;
import com.project.evgo.sharedkernel.enums.StationStatus;
import com.project.evgo.station.StationService;
import com.project.evgo.station.request.CreateStationRequest;
import com.project.evgo.station.request.SearchNearbyRequest;
import com.project.evgo.station.request.SearchTextRequest;
import com.project.evgo.station.request.StationFilterRequest;
import com.project.evgo.station.request.UpdateStationRequest;
import com.project.evgo.station.response.StationMetadataResponse;
import com.project.evgo.station.response.StationResponse;
import com.project.evgo.station.response.StationSearchResult;
import com.project.evgo.sharedkernel.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for station management.
 */
@RestController
@RequestMapping("/api/v1/stations")
@RequiredArgsConstructor
@Tag(name = "Stations", description = "Station management APIs")
public class StationController {

	private final StationService stationService;

	// ==================== CRUD ENDPOINTS ====================

	@GetMapping
	@Operation(summary = "Get all stations", description = "Get all active stations (public)")
	public ResponseEntity<ApiResponse<List<StationResponse>>> getAll() {
		List<StationResponse> result = stationService.findAll();
		return ResponseEntity.ok(ApiResponse.<List<StationResponse>>builder()
				.status(HttpStatus.OK.value())
				.message("Success")
				.data(result)
				.build());
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get station by ID", description = "Get station details by ID (public)")
	public ResponseEntity<ApiResponse<StationResponse>> getById(@PathVariable Long id) {
		StationResponse result = stationService.findById(id)
				.orElseThrow(() -> new com.project.evgo.sharedkernel.exceptions.AppException(
						com.project.evgo.sharedkernel.enums.ErrorCode.STATION_NOT_FOUND));
		return ResponseEntity.ok(ApiResponse.<StationResponse>builder()
				.status(HttpStatus.OK.value())
				.message("Success")
				.data(result)
				.build());
	}

	@GetMapping("/me")
	@PreAuthorize("hasRole('STATION_OWNER')")
	@Operation(summary = "Get my stations", description = "Get all stations owned by the current user")
	public ResponseEntity<ApiResponse<List<StationResponse>>> getMyStations() {
		List<StationResponse> result = stationService.getMyStations();
		return ResponseEntity.ok(ApiResponse.<List<StationResponse>>builder()
				.status(HttpStatus.OK.value())
				.message("Success")
				.data(result)
				.build());
	}

	@PostMapping
	@PreAuthorize("hasRole('STATION_OWNER')")
	@Operation(summary = "Create station", description = "Create a new charging station")
	public ResponseEntity<ApiResponse<StationResponse>> create(
			@Valid @RequestBody CreateStationRequest request) {
		StationResponse result = stationService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.<StationResponse>builder()
						.status(HttpStatus.CREATED.value())
						.message("Station created successfully")
						.data(result)
						.build());
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('STATION_OWNER')")
	@Operation(summary = "Update station", description = "Update an existing station (owner only)")
	public ResponseEntity<ApiResponse<StationResponse>> update(
			@PathVariable Long id,
			@Valid @RequestBody UpdateStationRequest request) {
		StationResponse result = stationService.update(id, request);
		return ResponseEntity.ok(ApiResponse.<StationResponse>builder()
				.status(HttpStatus.OK.value())
				.message("Station updated successfully")
				.data(result)
				.build());
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('STATION_OWNER')")
	@Operation(summary = "Delete station", description = "Soft delete a station (owner only)")
	public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
		stationService.delete(id);
		return ResponseEntity.ok(ApiResponse.<Void>builder()
				.status(HttpStatus.OK.value())
				.message("Station deleted successfully")
				.build());
	}

	@PatchMapping("/{id}/status")
	@PreAuthorize("hasRole('STATION_OWNER')")
	@Operation(summary = "Update station status", description = "Update station status (owner only)")
	public ResponseEntity<ApiResponse<StationResponse>> updateStatus(
			@PathVariable Long id,
			@RequestParam StationStatus status) {
		StationResponse result = stationService.updateStatus(id, status);
		return ResponseEntity.ok(ApiResponse.<StationResponse>builder()
				.status(HttpStatus.OK.value())
				.message("Station status updated successfully")
				.data(result)
				.build());
	}

	// ==================== SEARCH ENDPOINTS (PUBLIC) ====================

	@GetMapping("/search/nearby")
	@Operation(summary = "Search nearby stations", description = "Find charging stations within a radius of GPS coordinates")
	public ResponseEntity<ApiResponse<List<StationSearchResult>>> searchNearby(
			@RequestParam @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
			@RequestParam @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude,
			@RequestParam(defaultValue = "5.0") @DecimalMin("0.1") @DecimalMax("50.0") Double radiusKm,
			@RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer maxResults) {

		SearchNearbyRequest request = new SearchNearbyRequest(latitude, longitude, radiusKm, maxResults);
		List<StationSearchResult> results = stationService.searchNearby(request);

		String message = String.format("Found %d station(s)", results.size());
		return ResponseEntity.ok(ApiResponse.<List<StationSearchResult>>builder()
				.status(HttpStatus.OK.value())
				.message(message)
				.data(results)
				.build());
	}

	@GetMapping("/search/text")
	@Operation(summary = "Search stations by text", description = "Search stations by name, address, or description with optional location-based sorting")
	public ResponseEntity<ApiResponse<List<StationSearchResult>>> searchByText(
			@RequestParam @NotBlank String query,
			@RequestParam(required = false) @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
			@RequestParam(required = false) @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude,
			@RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer maxResults) {

		SearchTextRequest request = new SearchTextRequest(query, latitude, longitude, maxResults);
		List<StationSearchResult> results = stationService.searchByText(request);

		String message = String.format("Found %d station(s)", results.size());
		return ResponseEntity.ok(ApiResponse.<List<StationSearchResult>>builder()
				.status(HttpStatus.OK.value())
				.message(message)
				.data(results)
				.build());
	}

	@GetMapping("/in-bound")
	@Operation(summary = "Find stations in bounding box", description = "Find charging stations within a map bounding box (viewport) with optional distance calculation")
	public ResponseEntity<ApiResponse<List<StationSearchResult>>> findStationsInBound(
			@RequestParam @DecimalMin("-90.0") @DecimalMax("90.0") Double minLat,
			@RequestParam @DecimalMin("-90.0") @DecimalMax("90.0") Double maxLat,
			@RequestParam @DecimalMin("-180.0") @DecimalMax("180.0") Double minLng,
			@RequestParam @DecimalMin("-180.0") @DecimalMax("180.0") Double maxLng,
			@RequestParam(required = false) @DecimalMin("-90.0") @DecimalMax("90.0") Double userLat,
			@RequestParam(required = false) @DecimalMin("-180.0") @DecimalMax("180.0") Double userLng,
			@RequestParam(defaultValue = "20") @Min(1) @Max(500) Integer maxResults) {

		List<StationSearchResult> results = stationService.findStationsInBound(minLat, maxLat, minLng, maxLng,
				userLat, userLng, maxResults);

		String message = String.format("Found %d station(s) in bounding box", results.size());
		return ResponseEntity.ok(ApiResponse.<List<StationSearchResult>>builder()
				.status(HttpStatus.OK.value())
				.message(message)
				.data(results)
				.build());
	}

	// ==================== METADATA & FILTER ENDPOINTS (PUBLIC)
	// ====================

	@GetMapping("/metadata")
	@Operation(summary = "Get station filter metadata", description = "Returns power range (min/max), distinct connector types, and all station statuses needed to build the filter UI")
	public ResponseEntity<ApiResponse<StationMetadataResponse>> getMetadata() {
		StationMetadataResponse result = stationService.getMetadata();
		return ResponseEntity.ok(ApiResponse.<StationMetadataResponse>builder()
				.status(HttpStatus.OK.value())
				.message("Success")
				.data(result)
				.build());
	}

	@GetMapping("/filter")
	@Operation(summary = "Filter stations", description = "Dynamically filter stations by power range, connector type, and/or status. All parameters are optional.")
	public ResponseEntity<ApiResponse<PageResponse<StationSearchResult>>> filterStations(
			@Parameter(description = "Minimum charger power output in kW") @RequestParam(required = false) Double minPower,
			@Parameter(description = "Maximum charger power output in kW") @RequestParam(required = false) Double maxPower,
			@Parameter(description = "List of connector type values (e.g. IEC_TYPE_2, VINFAST_STD)") @RequestParam(required = false) List<String> connectorTypes,
			@Parameter(description = "Station status (ACTIVE, INACTIVE, PENDING, SUSPENDED)") @RequestParam(required = false) StationStatus status,
			@Parameter(description = "Search query by name or address") @RequestParam(required = false) String query,
			@Parameter(description = "User latitude for distance calculation") @RequestParam(required = false) Double userLat,
			@Parameter(description = "User longitude for distance calculation") @RequestParam(required = false) Double userLng,
			@Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") Integer page,
			@Parameter(description = "Page size") @RequestParam(defaultValue = "10") Integer size) {

		StationFilterRequest request = new StationFilterRequest(minPower, maxPower, connectorTypes, status, page, size,
				query, userLat, userLng);
		PageResponse<StationSearchResult> results = stationService.filterStations(request);

		String message = String.format("Found %d station(s)", results.totalElements());
		return ResponseEntity.ok(ApiResponse.<PageResponse<StationSearchResult>>builder()
				.status(HttpStatus.OK.value())
				.message(message)
				.data(results)
				.build());
	}
}
