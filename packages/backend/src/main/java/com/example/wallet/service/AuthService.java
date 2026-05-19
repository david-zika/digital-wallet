package com.example.wallet.service;

import com.example.wallet.dto.AuthRequest;
import com.example.wallet.dto.AuthResponse;
import com.example.wallet.dto.ChangePasswordRequest;
import com.example.wallet.dto.RegisterRequest;
import com.example.exception.WalletErrorCode;
import com.example.exception.WalletException;
import com.example.wallet.model.User;
import com.example.wallet.repository.UserRepository;
import com.example.wallet.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new WalletException(WalletErrorCode.EMAIL_ALREADY_EXISTS);
        }

        String accountReference = "ACC-" + UUID.randomUUID().toString()
                .substring(0, 8).toUpperCase();

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setAccountReference(accountReference);
        user.setFullName(request.getFullName());
        user.setBankAccount(request.getBankAccount());
        userRepository.save(user);

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("accountReference", accountReference);
        return new AuthResponse(jwtService.generateToken(extraClaims, user));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(AuthRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (BadCredentialsException exception) {
            throw new WalletException(WalletErrorCode.INVALID_CREDENTIALS);
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new WalletException(WalletErrorCode.INVALID_CREDENTIALS));

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("accountReference", user.getAccountReference());
        return new AuthResponse(jwtService.generateToken(extraClaims, user));
    }

    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new WalletException(WalletErrorCode.RESOURCE_NOT_FOUND));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new WalletException(WalletErrorCode.INVALID_CREDENTIALS);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}