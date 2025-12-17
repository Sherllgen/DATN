package com.project.evgo.user.internal.web;

import com.project.evgo.sharedkernel.dto.ApiResponse;
import com.project.evgo.user.UserService;
import com.project.evgo.user.request.UpdateProfileRequest;
import com.project.evgo.user.request.ChangePasswordRequest;
import com.project.evgo.user.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management APIs")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

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

    // @PostMapping("/me/avatar")
    // @Operation(summary = "Upload user avatar")
    // public ResponseEntity<UserResponse> uploadAvatar(
    // @AuthenticationPrincipal UserDetails userDetails,
    // @RequestParam("file") MultipartFile file) {
    // UserResponse response = userService.uploadAvatar(userDetails.getUsername(),
    // file);
    // return ResponseEntity.ok(response);
    // }
}
