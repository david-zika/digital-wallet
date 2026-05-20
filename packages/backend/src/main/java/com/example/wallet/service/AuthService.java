package com.example.wallet.service;

import com.example.wallet.dto.AuthRequest;
import com.example.wallet.dto.ChangePasswordRequest;
import com.example.wallet.dto.RefreshTokenRequest;
import com.example.wallet.dto.RegisterRequest;
import com.example.wallet.dto.TokenPairResponse;
import com.example.wallet.exception.WalletErrorCode;
import com.example.wallet.exception.WalletException;
import com.example.wallet.model.RefreshToken;
import com.example.wallet.model.User;
import com.example.wallet.repository.RefreshTokenRepository;
import com.example.wallet.repository.UserRepository;
import com.example.wallet.security.JwtService;
import com.example.wallet.security.UserPrincipal;
import com.example.wallet.service.port.AuthServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService implements AuthServicePort {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    @Override
    @Transactional
    public TokenPairResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new WalletException(WalletErrorCode.EMAIL_ALREADY_EXISTS);
        }
        String accountReference = "ACC-" + UUID.randomUUID().toString()
                .substring(0, 8).toUpperCase();
        User user = new User();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setAccountReference(accountReference);
        user.setFullName(request.fullName());
        user.setBankAccount(request.bankAccount());
        userRepository.save(user);

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("accountReference", accountReference);
        String accessToken = jwtService.generateToken(extraClaims, new UserPrincipal(user));
        String refreshTokenValue = createRefreshToken(user);
        return new TokenPairResponse(accessToken, refreshTokenValue);
    }

    @Override
    @Transactional
    public TokenPairResponse login(AuthRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (BadCredentialsException exception) {
            throw new WalletException(WalletErrorCode.INVALID_CREDENTIALS);
        }
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new WalletException(WalletErrorCode.INVALID_CREDENTIALS));

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("accountReference", user.getAccountReference());
        String accessToken = jwtService.generateToken(extraClaims, new UserPrincipal(user));
        String refreshTokenValue = createRefreshToken(user);
        return new TokenPairResponse(accessToken, refreshTokenValue);
    }

    @Override
    @Transactional
    public TokenPairResponse refresh(RefreshTokenRequest request) {
        RefreshToken stored = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new WalletException(WalletErrorCode.UNAUTHORIZED, "Invalid refresh token"));
        if (stored.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenRepository.delete(stored);
            throw new WalletException(WalletErrorCode.UNAUTHORIZED, "Refresh token expired");
        }
        User user = stored.getUser();
        refreshTokenRepository.delete(stored);

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("accountReference", user.getAccountReference());
        String newAccessToken = jwtService.generateToken(extraClaims, new UserPrincipal(user));
        String newRefreshToken = createRefreshToken(user);
        return new TokenPairResponse(newAccessToken, newRefreshToken);
    }

    @Override
    @Transactional
    public void logout(String refreshTokenValue) {
        refreshTokenRepository.findByToken(refreshTokenValue)
                .ifPresent(refreshTokenRepository::delete);
    }

    @Override
    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new WalletException(WalletErrorCode.RESOURCE_NOT_FOUND));
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new WalletException(WalletErrorCode.INVALID_CREDENTIALS);
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        refreshTokenRepository.deleteAllByUserId(userId);
    }

    @Scheduled(fixedDelay = 3_600_000)
    @Transactional
    public void evictExpiredRefreshTokens() {
        refreshTokenRepository.deleteAllExpired(Instant.now());
    }

    private String createRefreshToken(User user) {
        RefreshToken rt = new RefreshToken();
        rt.setUser(user);
        rt.setToken(UUID.randomUUID().toString());
        rt.setExpiresAt(Instant.now().plusMillis(refreshExpiration));
        refreshTokenRepository.save(rt);
        return rt.getToken();
    }
}
