package com.project.evgo.user.internal.web;

import com.project.evgo.sharedkernel.dto.ApiResponse;
import com.project.evgo.user.CloudinaryService;
import com.project.evgo.user.UserService;
import com.project.evgo.user.request.UpdateAvatarRequest;
import com.project.evgo.user.request.UpdateProfileRequest;
import com.project.evgo.user.request.ChangePasswordRequest;
import com.project.evgo.user.response.UploadSignatureResponse;
import com.project.evgo.user.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management APIs")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    private final CloudinaryService cloudinaryService;

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        UserResponse response = userService.getCurrentUser();
        return ResponseEntity.ok(new ApiResponse<>(200, "Get profile successfully", response));
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request) {
        UserResponse response = userService.updateProfile(request);
        return ResponseEntity.ok(new ApiResponse<>(200, "Profile updated successfully", response));
    }

    @PutMapping("/me/password")
    @Operation(summary = "Change password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return ResponseEntity.ok(new ApiResponse<>(200, "Password changed successfully", null));
    }

    @PostMapping("/me/avatar/upload-signature")
    @Operation(summary = "Get Cloudinary upload signature for avatar")
    public ResponseEntity<ApiResponse<?>> getAvatarUploadSignature(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>(401, "Unauthorized", null));
            }
            Map<String, String> signature = cloudinaryService.generateUploadSignature();

            UploadSignatureResponse uploadSignatureResponse = new UploadSignatureResponse(
                    signature.get("cloudName"),
                    signature.get("apiKey"),
                    signature.get("timestamp"),
                    signature.get("signature"),
                    signature.get("folder")
            );
            return ResponseEntity.ok(new ApiResponse<>(200, "Upload signature successfully", uploadSignatureResponse));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Failed to generate upload signature: " + e.getMessage(), null));
        }
    }

    @PostMapping("/me/avatar")
    @Operation(summary = "Update user avatar")
    public ResponseEntity<ApiResponse<UserResponse>> updateAvatar(
            Authentication authentication,
            @Valid @RequestBody UpdateAvatarRequest request) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>(401, "Unauthorized", null));
            }
            UserResponse currentUser = userService.getCurrentUser();
            Long userId = currentUser.id();
            UserResponse response = userService.updateAvatar(userId, request.avatarUrl(), request.publicId());
            return ResponseEntity.ok(new ApiResponse<>(200, "Avatar updated successfully", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Failed to update avatar: " + e.getMessage(), null));
        }
    }

}
