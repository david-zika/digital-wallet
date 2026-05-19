package com.example.wallet.controller;

import com.example.wallet.dto.ProfileResponse;
import com.example.wallet.dto.UpdateProfileRequest;
import com.example.wallet.model.User;
import com.example.wallet.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/profile")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "User profile management API")
public class ProfileController {
    private final ProfileService profileService;

    @Operation(
        summary = "Get user profile",
        description = "Retrieves the current user's profile information"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Profile retrieved successfully",
            content = @Content(schema = @Schema(implementation = ProfileResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile(
            @AuthenticationPrincipal User userDetails) {
        return ResponseEntity.ok(profileService.getProfile(userDetails.getId()));
    }

    @Operation(
        summary = "Update user profile",
        description = "Updates the current user's profile information"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Profile updated successfully",
            content = @Content(schema = @Schema(implementation = ProfileResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PutMapping
    public ResponseEntity<ProfileResponse> updateProfile(
            @AuthenticationPrincipal User userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(profileService.updateProfile(userDetails.getId(), request));
    }
}