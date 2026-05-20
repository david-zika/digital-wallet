package com.example.wallet.service;

import com.example.wallet.dto.AuthRequest;
import com.example.wallet.dto.ChangePasswordRequest;
import com.example.wallet.dto.RegisterRequest;
import com.example.wallet.dto.TokenPairResponse;
import com.example.wallet.exception.WalletException;
import com.example.wallet.model.RefreshToken;
import com.example.wallet.model.User;
import com.example.wallet.repository.RefreshTokenRepository;
import com.example.wallet.repository.UserRepository;
import com.example.wallet.security.JwtService;
import com.example.wallet.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "refreshExpiration", 604_800_000L);

        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("user@example.com");
        testUser.setPassword("{bcrypt}hashed");
        testUser.setAccountReference("ACC-ABCD1234");
        testUser.setFullName("Test User");
    }

    @Test
    void register_shouldSaveUserAndReturnTokenPair() {
        RegisterRequest req = new RegisterRequest("user@example.com", "password123", "Test User", null);
        when(userRepository.existsByEmail(req.email())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("{bcrypt}hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateToken(any(), any(UserPrincipal.class))).thenReturn("access-token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        TokenPairResponse result = authService.register(req);

        assertThat(result.token()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isNotBlank();
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_shouldThrowWhenEmailTaken() {
        RegisterRequest req = new RegisterRequest("user@example.com", "password123", "Test User", null);
        when(userRepository.existsByEmail(req.email())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(WalletException.class);
    }

    @Test
    void login_shouldReturnTokenPairOnValidCredentials() {
        AuthRequest req = new AuthRequest("user@example.com", "password123");
        when(userRepository.findByEmail(req.email())).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(any(), any(UserPrincipal.class))).thenReturn("access-token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        TokenPairResponse result = authService.login(req);

        assertThat(result.token()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isNotBlank();
    }

    @Test
    void login_shouldThrowOnBadCredentials() {
        AuthRequest req = new AuthRequest("user@example.com", "wrong");
        doThrow(new BadCredentialsException("bad"))
                .when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(WalletException.class);
    }

    @Test
    void changePassword_shouldUpdatePasswordAndRevokeRefreshTokens() {
        ChangePasswordRequest req = new ChangePasswordRequest("oldPass", "newPass123");
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldPass", testUser.getPassword())).thenReturn(true);
        when(passwordEncoder.encode("newPass123")).thenReturn("{bcrypt}newhash");

        authService.changePassword(testUser.getId(), req);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("{bcrypt}newhash");
        verify(refreshTokenRepository).deleteAllByUserId(testUser.getId());
    }

    @Test
    void changePassword_shouldThrowWhenCurrentPasswordWrong() {
        ChangePasswordRequest req = new ChangePasswordRequest("wrong", "newPass123");
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrong", testUser.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> authService.changePassword(testUser.getId(), req))
                .isInstanceOf(WalletException.class);
    }

    @Test
    void logout_shouldDeleteRefreshToken() {
        RefreshToken rt = new RefreshToken();
        rt.setToken("some-refresh-token");
        when(refreshTokenRepository.findByToken("some-refresh-token")).thenReturn(Optional.of(rt));

        authService.logout("some-refresh-token");

        verify(refreshTokenRepository).delete(rt);
    }

    @Test
    void evictExpiredRefreshTokens_shouldCallRepository() {
        authService.evictExpiredRefreshTokens();

        verify(refreshTokenRepository).deleteAllExpired(any(Instant.class));
    }
}

