package com.example.wallet.security;

import com.example.wallet.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtService jwtService;
    private UserPrincipal userPrincipal;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey",
                "test-secret-key-must-be-at-least-32-bytes-long-for-hs256");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 900_000L);
        ReflectionTestUtils.setField(jwtService, "jwtIssuer", "digital-wallet-api");
        ReflectionTestUtils.setField(jwtService, "jwtAudience", "digital-wallet-client");

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");
        user.setPassword("hashed");
        user.setAccountReference("ACC-ABCD1234");
        userPrincipal = new UserPrincipal(user);
    }

    @Test
    void generateToken_shouldContainCorrectSubject() {
        String token = jwtService.generateToken(userPrincipal);

        assertThat(jwtService.extractUsername(token)).isEqualTo("test@example.com");
    }

    @Test
    void generateToken_shouldBeValidForSamePrincipal() {
        String token = jwtService.generateToken(userPrincipal);

        assertThat(jwtService.isTokenValid(token, userPrincipal)).isTrue();
    }

    @Test
    void generateToken_shouldIncludeExtraClaims() {
        var claims = new HashMap<String, Object>();
        claims.put("accountReference", "ACC-ABCD1234");

        String token = jwtService.generateToken(claims, userPrincipal);

        String accountRef = jwtService.extractClaim(token,
                c -> c.get("accountReference", String.class));
        assertThat(accountRef).isEqualTo("ACC-ABCD1234");
    }

    @Test
    void isTokenValid_shouldReturnFalseForDifferentUser() {
        String token = jwtService.generateToken(userPrincipal);

        User other = new User();
        other.setId(UUID.randomUUID());
        other.setEmail("other@example.com");
        other.setPassword("hashed");
        other.setAccountReference("ACC-00000000");
        UserPrincipal otherPrincipal = new UserPrincipal(other);

        assertThat(jwtService.isTokenValid(token, otherPrincipal)).isFalse();
    }

    @Test
    void generateToken_withExpiredExpiration_shouldBeInvalid() {
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", -1000L);

        String token = jwtService.generateToken(userPrincipal);

        assertThatThrownBy(() -> jwtService.extractUsername(token))
                .isInstanceOf(Exception.class);
    }

    @Test
    void generateToken_withWrongSecret_shouldFailValidation() {
        String token = jwtService.generateToken(userPrincipal);

        JwtService otherService = new JwtService();
        ReflectionTestUtils.setField(otherService, "secretKey",
                "completely-different-secret-key-32bytes-long-x!!");
        ReflectionTestUtils.setField(otherService, "jwtExpiration", 900_000L);
        ReflectionTestUtils.setField(otherService, "jwtIssuer", "digital-wallet-api");
        ReflectionTestUtils.setField(otherService, "jwtAudience", "digital-wallet-client");

        assertThatThrownBy(() -> otherService.extractUsername(token))
                .isInstanceOf(Exception.class);
    }

    @Test
    void generateToken_eachCallShouldProduceDifferentJti() {
        String token1 = jwtService.generateToken(userPrincipal);
        String token2 = jwtService.generateToken(userPrincipal);

        String jti1 = jwtService.extractClaim(token1, claims -> claims.getId());
        String jti2 = jwtService.extractClaim(token2, claims -> claims.getId());

        assertThat(jti1).isNotEqualTo(jti2);
    }
}

