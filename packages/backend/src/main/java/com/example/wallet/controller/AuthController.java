package com.example.wallet.controller;

import com.example.wallet.dto.AuthRequest;
import com.example.wallet.dto.AuthResponse;
import com.example.wallet.dto.ChangePasswordRequest;
import com.example.wallet.dto.RegisterRequest;
import com.example.wallet.model.User;
import com.example.wallet.service.AuthService;
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
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication management API")
public class AuthController {
    private final AuthService authService;

    @Operation(
        summary = "Register new user",
        description = "Creates a new user account with the provided email and password"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User successfully registered",
            content = @Content(schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @Operation(
        summary = "User login",
        description = "Authenticates user with email and password"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully authenticated",
            content = @Content(schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(
        summary = "Change password",
        description = "Changes the user's password"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Password successfully changed"),
        @ApiResponse(responseCode = "400", description = "Invalid password"),
        @ApiResponse(responseCode = "401", description = "Current password is incorrect")
    })
    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal User userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(userDetails.getId(), request);
        return ResponseEntity.ok().build();
    }
}