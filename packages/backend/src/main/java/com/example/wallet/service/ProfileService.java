package com.example.wallet.service;

import com.example.wallet.dto.ProfileResponse;
import com.example.wallet.dto.UpdateProfileRequest;
import com.example.exception.WalletErrorCode;
import com.example.exception.WalletException;
import com.example.wallet.model.User;
import com.example.wallet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new WalletException(WalletErrorCode.RESOURCE_NOT_FOUND));

        return new ProfileResponse(
                user.getEmail(),
                user.getAccountReference(),
                user.getFullName(),
                user.getBankAccount()
        );
    }

    @Transactional
    public ProfileResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new WalletException(WalletErrorCode.RESOURCE_NOT_FOUND));

        user.setFullName(request.getFullName());
        user.setBankAccount(request.getBankAccount());
        userRepository.save(user);

        return new ProfileResponse(
                user.getEmail(),
                user.getAccountReference(),
                user.getFullName(),
                user.getBankAccount()
        );
    }
}