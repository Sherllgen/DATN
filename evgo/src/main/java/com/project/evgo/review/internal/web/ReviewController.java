package com.project.evgo.review.internal.web;

import com.project.evgo.review.ReviewService;
import com.project.evgo.review.request.CreateReviewRequest;
import com.project.evgo.review.request.UpdateReviewRequest;
import com.project.evgo.review.response.ReviewResponse;
import com.project.evgo.review.response.StationReviewsSummaryResponse;
import com.project.evgo.sharedkernel.dto.ApiResponse;
import com.project.evgo.sharedkernel.dto.PageResponse;
import com.project.evgo.sharedkernel.enums.ErrorCode;
import com.project.evgo.sharedkernel.exceptions.AppException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for review management.
 */
@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Station review APIs")
public class ReviewController {

        private final ReviewService reviewService;

        @GetMapping("/{id}")
        @Operation(summary = "Get review by ID")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Review found"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Review not found")
        })
        public ResponseEntity<ApiResponse<ReviewResponse>> getById(
                        @Parameter(description = "Review ID") @PathVariable Long id) {
                ReviewResponse result = reviewService.findById(id)
                                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));
                return ResponseEntity.ok(ApiResponse.<ReviewResponse>builder()
                                .status(HttpStatus.OK.value())
                                .message("Success")
                                .data(result)
                                .build());
        }

        @GetMapping("/user/{userId}")
        @Operation(summary = "Get reviews by user ID")
        public ResponseEntity<ApiResponse<List<ReviewResponse>>> getByUserId(
                        @Parameter(description = "User ID") @PathVariable Long userId) {
                List<ReviewResponse> result = reviewService.findByUserId(userId);
                return ResponseEntity.ok(ApiResponse.<List<ReviewResponse>>builder()
                                .status(HttpStatus.OK.value())
                                .message("Success")
                                .data(result)
                                .build());
        }

        // =================== Station-Scoped Endpoints ===================

        @GetMapping("/station/{stationId}/summary")
        @Operation(summary = "Get review summary for a station", description = "Returns average rating, total reviews, and per-star distribution")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Summary retrieved")
        })
        public ResponseEntity<ApiResponse<StationReviewsSummaryResponse>> getReviewSummary(
                        @Parameter(description = "Station ID") @PathVariable Long stationId) {
                StationReviewsSummaryResponse result = reviewService.getReviewSummary(stationId);
                return ResponseEntity.ok(ApiResponse.<StationReviewsSummaryResponse>builder()
                                .status(HttpStatus.OK.value())
                                .message("Success")
                                .data(result)
                                .build());
        }

        @GetMapping("/station/{stationId}")
        @Operation(summary = "Get paginated reviews for a station", description = "Supports pagination and sorting (e.g. ?page=0&size=10&sort=createdAt,desc)")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reviews retrieved")
        })
        public ResponseEntity<ApiResponse<PageResponse<ReviewResponse>>> getStationReviews(
                        @Parameter(description = "Station ID") @PathVariable Long stationId,
                        @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
                        @Parameter(description = "Sort field and direction (e.g. createdAt,desc)") @RequestParam(defaultValue = "createdAt,desc") String sort) {
                String[] sortParts = sort.split(",");
                Sort.Direction direction = sortParts.length > 1 && sortParts[1].equalsIgnoreCase("asc")
                                ? Sort.Direction.ASC
                                : Sort.Direction.DESC;
                Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParts[0]));
                PageResponse<ReviewResponse> result = reviewService.getStationReviews(stationId, pageable);
                return ResponseEntity.ok(ApiResponse.<PageResponse<ReviewResponse>>builder()
                                .status(HttpStatus.OK.value())
                                .message("Success")
                                .data(result)
                                .build());
        }

        @PostMapping("/station/{stationId}")
        @PreAuthorize("hasRole('USER')")
        @Operation(summary = "Submit a review for a station", description = "Authenticated users only. Rating must be 1–5.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Review created"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
        })
        public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
                        @Parameter(description = "Station ID") @PathVariable Long stationId,
                        @Valid @RequestBody CreateReviewRequest request) {
                ReviewResponse result = reviewService.createReview(stationId, request);
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.<ReviewResponse>builder()
                                                .status(HttpStatus.CREATED.value())
                                                .message("Review created successfully")
                                                .data(result)
                                                .build());
        }

        @PutMapping("/{id}")
        @PreAuthorize("hasRole('USER')")
        @Operation(summary = "Update an existing review", description = "Only the owner can update their own review.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Review updated"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Not the owner"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Review not found")
        })
        public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
                        @Parameter(description = "Review ID") @PathVariable Long id,
                        @Valid @RequestBody UpdateReviewRequest request) {
                ReviewResponse result = reviewService.updateReview(id, request);
                return ResponseEntity.ok(ApiResponse.<ReviewResponse>builder()
                                .status(HttpStatus.OK.value())
                                .message("Review updated successfully")
                                .data(result)
                                .build());
        }

        @DeleteMapping("/{id}")
        @PreAuthorize("hasAnyRole('USER', 'SUPER_ADMIN')")
        @Operation(summary = "Delete a review", description = "Only the owner can delete their own review.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Review deleted"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Not the owner"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Review not found")
        })
        public ResponseEntity<ApiResponse<Void>> deleteReview(
                        @Parameter(description = "Review ID") @PathVariable Long id) {
                reviewService.deleteReview(id);
                return ResponseEntity.status(HttpStatus.NO_CONTENT)
                                .body(ApiResponse.<Void>builder()
                                                .status(HttpStatus.NO_CONTENT.value())
                                                .message("Review deleted successfully")
                                                .build());
        }
}
