package com.project.evgo.review.internal.web;

import com.project.evgo.review.ReviewService;
import com.project.evgo.review.response.ReviewResponse;
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
 * REST controller for review management.
 */
@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Review management APIs")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/{id}")
    @Operation(summary = "Get review by ID")
    public ResponseEntity<ApiResponse<ReviewResponse>> getById(@PathVariable Long id) {
        var result = reviewService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        return ResponseEntity.ok(ApiResponse.<ReviewResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Success")
                .data(result)
                .build());
    }

    @GetMapping("/station/{stationId}")
    @Operation(summary = "Get reviews by station ID")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getByStationId(
            @PathVariable Long stationId) {
        var result = reviewService.findByStationId(stationId);
        return ResponseEntity.ok(ApiResponse.<List<ReviewResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Success")
                .data(result)
                .build());
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get reviews by user ID")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getByUserId(
            @PathVariable Long userId) {
        var result = reviewService.findByUserId(userId);
        return ResponseEntity.ok(ApiResponse.<List<ReviewResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Success")
                .data(result)
                .build());
    }

    @GetMapping("/station/{stationId}/rating")
    @Operation(summary = "Get average rating by station ID")
    public ResponseEntity<ApiResponse<Double>> getAverageRating(@PathVariable Long stationId) {
        var result = reviewService.getAverageRatingByStationId(stationId);
        return ResponseEntity.ok(ApiResponse.<Double>builder()
                .status(HttpStatus.OK.value())
                .message("Success")
                .data(result)
                .build());
    }
}
