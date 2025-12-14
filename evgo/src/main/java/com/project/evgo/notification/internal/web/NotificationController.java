package com.project.evgo.notification.internal.web;

import com.project.evgo.notification.NotificationService;
import com.project.evgo.notification.response.NotificationResponse;
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
 * REST controller for notification management.
 */
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notification management APIs")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/{id}")
    @Operation(summary = "Get notification by ID")
    public ResponseEntity<ApiResponse<NotificationResponse>> getById(@PathVariable Long id) {
        var result = notificationService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        return ResponseEntity.ok(ApiResponse.<NotificationResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Success")
                .data(result)
                .build());
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get notifications by user ID")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getByUserId(
            @PathVariable Long userId) {
        var result = notificationService.findByUserId(userId);
        return ResponseEntity.ok(ApiResponse.<List<NotificationResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Success")
                .data(result)
                .build());
    }

    @GetMapping("/user/{userId}/unread")
    @Operation(summary = "Get unread notifications by user ID")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getUnreadByUserId(
            @PathVariable Long userId) {
        var result = notificationService.findUnreadByUserId(userId);
        return ResponseEntity.ok(ApiResponse.<List<NotificationResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Success")
                .data(result)
                .build());
    }

    @GetMapping("/user/{userId}/unread/count")
    @Operation(summary = "Count unread notifications by user ID")
    public ResponseEntity<ApiResponse<Long>> countUnreadByUserId(@PathVariable Long userId) {
        var result = notificationService.countUnreadByUserId(userId);
        return ResponseEntity.ok(ApiResponse.<Long>builder()
                .status(HttpStatus.OK.value())
                .message("Success")
                .data(result)
                .build());
    }
}
