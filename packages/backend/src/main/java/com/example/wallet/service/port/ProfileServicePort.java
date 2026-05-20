package com.example.wallet.service.port;

import com.example.wallet.dto.ProfileResponse;
import com.example.wallet.dto.UpdateProfileRequest;

import java.util.UUID;

public interface ProfileServicePort {
    ProfileResponse getProfile(UUID userId);
    ProfileResponse updateProfile(UUID userId, UpdateProfileRequest request);
}
