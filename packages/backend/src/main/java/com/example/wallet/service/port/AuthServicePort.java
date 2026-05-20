package com.example.wallet.service.port;

import com.example.wallet.dto.AuthRequest;
import com.example.wallet.dto.ChangePasswordRequest;
import com.example.wallet.dto.RefreshTokenRequest;
import com.example.wallet.dto.RegisterRequest;
import com.example.wallet.dto.TokenPairResponse;

import java.util.UUID;

public interface AuthServicePort {
    TokenPairResponse register(RegisterRequest request);
    TokenPairResponse login(AuthRequest request);
    TokenPairResponse refresh(RefreshTokenRequest request);
    void logout(String refreshTokenValue);
    void changePassword(UUID userId, ChangePasswordRequest request);
}
