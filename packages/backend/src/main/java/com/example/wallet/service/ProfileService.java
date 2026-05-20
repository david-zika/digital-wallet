package com.example.wallet.service;

import com.example.wallet.dto.ProfileResponse;
import com.example.wallet.dto.UpdateProfileRequest;
import com.example.wallet.exception.WalletErrorCode;
import com.example.wallet.exception.WalletException;
import com.example.wallet.model.User;
import com.example.wallet.repository.UserRepository;
import com.example.wallet.service.port.ProfileServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService implements ProfileServicePort {

    private final UserRepository userRepository;

    @Override
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

    @Override
    @Transactional
    public ProfileResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new WalletException(WalletErrorCode.RESOURCE_NOT_FOUND));
        user.setFullName(request.fullName());
        user.setBankAccount(request.bankAccount());

        return new ProfileResponse(
                user.getEmail(),
                user.getAccountReference(),
                user.getFullName(),
                user.getBankAccount()
        );
    }
}