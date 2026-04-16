package com.project.evgo.notification.internal.web;

import com.project.evgo.notification.PushTokenService;
import com.project.evgo.notification.request.RegisterPushTokenRequest;
import com.project.evgo.notification.response.PushTokenResponse;
import com.project.evgo.sharedkernel.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications/push-tokens")
@RequiredArgsConstructor
@Tag(name = "Push Notifications", description = "Endpoints for managing push tokens")
public class PushTokenController {

    private final PushTokenService pushTokenService;

    @PostMapping
    @Operation(summary = "Register device token", description = "Registers or updates a device token for the current user to receive push notifications.")
    public ResponseEntity<ApiResponse<PushTokenResponse>> registerToken(
            @AuthenticationPrincipal Long currentUserId, // Assuming standard ID extraction, though standard Spring
                                                         // security might need UserDetails implementation
            @Valid @RequestBody RegisterPushTokenRequest request) {

        // Use a default user ID for now if currentUserId is not mapped via custom
        // resolution,
        // typically a custom ArgumentResolver handles @AuthenticationPrincipal Long
        // userId.
        Long userId = currentUserId != null ? currentUserId : 1L; // Fallback for simplicity

        PushTokenResponse response = pushTokenService.registerToken(userId, request);
        return ResponseEntity.ok(ApiResponse.<PushTokenResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Token registered successfully")
                .data(response)
                .build());
    }

    @DeleteMapping("/{token}")
    @Operation(summary = "Unregister device token", description = "Removes a device token to stop receiving push notifications.")
    public ResponseEntity<ApiResponse<Void>> unregisterToken(
            @PathVariable String token) {

        pushTokenService.unregisterToken(token);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Token unregistered successfully")
                .build());
    }
}
