package com.project.evgo.station.internal.web;

import com.project.evgo.sharedkernel.dto.ApiResponse;
import com.project.evgo.station.StationPhotoService;
import com.project.evgo.station.request.ReorderStationPhotosRequest;
import com.project.evgo.station.request.UpdateStationPhotoRequest;
import com.project.evgo.station.response.StationPhotoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST Controller for station photo management.
 */
@RestController
@RequestMapping("/api/v1/stations/{stationId}/photos")
@RequiredArgsConstructor
@Tag(name = "Station Photos", description = "Station photo management APIs")
public class StationPhotoController {

        private final StationPhotoService stationPhotoService;

        @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @PreAuthorize("hasRole('STATION_OWNER')")
        @Operation(summary = "Add photo", description = "Upload and add a photo to a station (owner only)")
        public ResponseEntity<ApiResponse<StationPhotoResponse>> addPhoto(
                        @PathVariable Long stationId,
                        @RequestPart("file") MultipartFile file,
                        @RequestParam(value = "caption", required = false) String caption) {
                StationPhotoResponse result = stationPhotoService.addPhoto(stationId, file, caption);
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.<StationPhotoResponse>builder()
                                                .status(HttpStatus.CREATED.value())
                                                .message("Photo added successfully")
                                                .data(result)
                                                .build());
        }

        @GetMapping
        @Operation(summary = "Get photos", description = "Get all photos for a station (public)")
        public ResponseEntity<ApiResponse<List<StationPhotoResponse>>> getPhotos(
                        @PathVariable Long stationId) {
                List<StationPhotoResponse> result = stationPhotoService.getPhotos(stationId);
                return ResponseEntity.ok(ApiResponse.<List<StationPhotoResponse>>builder()
                                .status(HttpStatus.OK.value())
                                .message("Success")
                                .data(result)
                                .build());
        }

        @PutMapping("/{photoId}")
        @PreAuthorize("hasRole('STATION_OWNER')")
        @Operation(summary = "Update photo", description = "Update photo metadata (owner only)")
        public ResponseEntity<ApiResponse<StationPhotoResponse>> updatePhoto(
                        @PathVariable Long stationId,
                        @PathVariable Long photoId,
                        @Valid @RequestBody UpdateStationPhotoRequest request) {
                StationPhotoResponse result = stationPhotoService.updatePhoto(photoId, request);
                return ResponseEntity.ok(ApiResponse.<StationPhotoResponse>builder()
                                .status(HttpStatus.OK.value())
                                .message("Photo updated successfully")
                                .data(result)
                                .build());
        }

        @DeleteMapping("/{photoId}")
        @PreAuthorize("hasRole('STATION_OWNER')")
        @Operation(summary = "Delete photo", description = "Delete a station photo (owner only)")
        public ResponseEntity<ApiResponse<Void>> deletePhoto(
                        @PathVariable Long stationId,
                        @PathVariable Long photoId) {
                stationPhotoService.deletePhoto(photoId);
                return ResponseEntity.ok(ApiResponse.<Void>builder()
                                .status(HttpStatus.OK.value())
                                .message("Photo deleted successfully")
                                .build());
        }

        @PutMapping("/reorder")
        @PreAuthorize("hasRole('STATION_OWNER')")
        @Operation(summary = "Reorder photos", description = "Reorder station photos (owner only)")
        public ResponseEntity<ApiResponse<List<StationPhotoResponse>>> reorderPhotos(
                        @PathVariable Long stationId,
                        @Valid @RequestBody ReorderStationPhotosRequest request) {
                List<StationPhotoResponse> result = stationPhotoService.reorderPhotos(stationId,
                                request.photoIdsInOrder());
                return ResponseEntity.ok(ApiResponse.<List<StationPhotoResponse>>builder()
                                .status(HttpStatus.OK.value())
                                .message("Photos reordered successfully")
                                .data(result)
                                .build());
        }
}
