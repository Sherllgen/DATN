package com.project.evgo.notification.internal.web;

import com.project.evgo.notification.PushTokenService;
import com.project.evgo.notification.request.RegisterPushTokenRequest;
import com.project.evgo.notification.response.PushTokenResponse;
import com.project.evgo.sharedkernel.dto.ApiResponse;
import com.project.evgo.sharedkernel.enums.ErrorCode;
import com.project.evgo.sharedkernel.exceptions.AppException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
            @Valid @RequestBody RegisterPushTokenRequest request) {

        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "User must be logged in to register push token");
        }

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

    // ------------------------ Helper ------------------------
    private Long getCurrentUserId() {
        org.springframework.security.core.Authentication authentication = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() != null) {
            Object principal = authentication.getPrincipal();
            try {
                Object idVal =  principal.getClass().getMethod("getId").invoke(principal);
                if (idVal instanceof Long) {
                    return (Long) idVal;
                }
            } catch (Exception e) {
                // Ignore and return null
            }
        }
        return null;
    }
}
