package com.example.wallet.controller;

import com.example.wallet.config.JpaAuditingConfig;
import com.example.wallet.dto.AuthRequest;
import com.example.wallet.dto.RefreshTokenRequest;
import com.example.wallet.dto.RegisterRequest;
import com.example.wallet.dto.TokenPairResponse;
import com.example.wallet.exception.GlobalExceptionHandler;
import com.example.wallet.exception.WalletErrorCode;
import com.example.wallet.exception.WalletException;
import com.example.wallet.security.JwtService;
import com.example.wallet.service.CustomUserDetailsService;
import com.example.wallet.service.port.AuthServicePort;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    value = AuthController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JpaAuditingConfig.class)
)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired MockMvc mvc;
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired ObjectMapper objectMapper;

    @MockitoBean AuthServicePort authService;
    @MockitoBean JwtService jwtService;
    @MockitoBean CustomUserDetailsService customUserDetailsService;
    @MockitoBean org.springframework.data.redis.core.StringRedisTemplate redisTemplate;


    @Test
    void register_validRequest_returns201WithTokenPair() throws Exception {
        RegisterRequest req = new RegisterRequest("new@example.com", "password123", "New User", null);
        TokenPairResponse pair = new TokenPairResponse("access-token", "refresh-token");
        when(authService.register(any())).thenReturn(pair);

        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    void register_duplicateEmail_returns409() throws Exception {
        RegisterRequest req = new RegisterRequest("taken@example.com", "password123", "User", null);
        when(authService.register(any()))
                .thenThrow(new WalletException(WalletErrorCode.EMAIL_ALREADY_EXISTS));

        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("EMAIL_ALREADY_EXISTS"));
    }

    @Test
    void register_invalidEmail_returns400() throws Exception {
        RegisterRequest req = new RegisterRequest("not-an-email", "password123", "User", null);

        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_validCredentials_returns200WithTokenPair() throws Exception {
        AuthRequest req = new AuthRequest("user@example.com", "password123");
        TokenPairResponse pair = new TokenPairResponse("access-token", "refresh-token");
        when(authService.login(any())).thenReturn(pair);

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("access-token"));
    }

    @Test
    void login_badCredentials_returns401() throws Exception {
        AuthRequest req = new AuthRequest("user@example.com", "wrong");
        when(authService.login(any()))
                .thenThrow(new WalletException(WalletErrorCode.INVALID_CREDENTIALS));

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_validToken_returns200WithNewPair() throws Exception {
        RefreshTokenRequest req = new RefreshTokenRequest("valid-refresh-token");
        TokenPairResponse newPair = new TokenPairResponse("new-access", "new-refresh");
        when(authService.refresh(any())).thenReturn(newPair);

        mvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("new-access"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh"));
    }

    @Test
    void logout_validToken_returns204() throws Exception {
        RefreshTokenRequest req = new RefreshTokenRequest("some-refresh-token");
        doNothing().when(authService).logout(any());

        mvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void changePassword_missingBody_returns400() throws Exception {
        mvc.perform(post("/api/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
