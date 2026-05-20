package com.example.wallet.integration;

import com.example.wallet.model.User;
import com.example.wallet.model.WalletBalance;
import com.example.wallet.repository.UserRepository;
import com.example.wallet.repository.WalletBalanceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Transactional
class WalletIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    @ServiceConnection(name = "redis")
    static GenericContainer<?> redis =
            new GenericContainer<>(DockerImageName.parse("redis:7-alpine")).withExposedPorts(6379);

    private final UserRepository userRepository;
    private final WalletBalanceRepository walletBalanceRepository;

    WalletIntegrationTest(UserRepository userRepository, WalletBalanceRepository walletBalanceRepository) {
        this.userRepository = userRepository;
        this.walletBalanceRepository = walletBalanceRepository;
    }

    @Test
    void flyway_shouldApplyAllMigrationsSuccessfully() {
        assertThat(userRepository.count()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void walletBalance_shouldPersistAndRetrieveByUserAndCurrency() {
        User user = createUser("balance-test@example.com");

        WalletBalance balance = new WalletBalance();
        balance.setUser(user);
        balance.setCurrency(WalletBalance.Currency.EUR);
        balance.setBalance(new BigDecimal("250.00"));
        walletBalanceRepository.save(balance);

        Optional<WalletBalance> found = walletBalanceRepository
                .findByUserIdAndCurrency(user.getId(), WalletBalance.Currency.EUR);

        assertThat(found).isPresent();
        assertThat(found.get().getBalance()).isEqualByComparingTo("250.00");
        assertThat(found.get().getId()).isNotNull();
    }

    @Test
    void walletBalance_idShouldBeUUID() {
        User user = createUser("uuid-id-test@example.com");

        WalletBalance balance = new WalletBalance();
        balance.setUser(user);
        balance.setCurrency(WalletBalance.Currency.CZK);
        balance.setBalance(BigDecimal.ZERO);
        WalletBalance saved = walletBalanceRepository.save(balance);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getId()).isInstanceOf(java.util.UUID.class);
    }

    @Test
    void walletBalance_pessimisticLock_shouldReturnBalance() {
        User user = createUser("lock-test@example.com");

        WalletBalance balance = new WalletBalance();
        balance.setUser(user);
        balance.setCurrency(WalletBalance.Currency.EUR);
        balance.setBalance(new BigDecimal("100.00"));
        walletBalanceRepository.save(balance);

        Optional<WalletBalance> locked = walletBalanceRepository
                .findByUserIdAndCurrencyWithLock(user.getId(), WalletBalance.Currency.EUR);

        assertThat(locked).isPresent();
        assertThat(locked.get().getBalance()).isEqualByComparingTo("100.00");
    }

    @Test
    void walletBalance_lastUpdated_shouldBeSetByAuditing() {
        User user = createUser("audit-test@example.com");

        WalletBalance balance = new WalletBalance();
        balance.setUser(user);
        balance.setCurrency(WalletBalance.Currency.EUR);
        balance.setBalance(BigDecimal.ZERO);
        WalletBalance saved = walletBalanceRepository.saveAndFlush(balance);

        assertThat(saved.getLastUpdated()).isNotNull();
    }

    @Test
    void walletBalance_findByUserId_shouldReturnAllCurrencies() {
        User user = createUser("multi-currency@example.com");

        for (WalletBalance.Currency currency : WalletBalance.Currency.values()) {
            WalletBalance wb = new WalletBalance();
            wb.setUser(user);
            wb.setCurrency(currency);
            wb.setBalance(BigDecimal.ZERO);
            walletBalanceRepository.save(wb);
        }

        List<WalletBalance> balances = walletBalanceRepository.findByUserId(user.getId());
        assertThat(balances).hasSize(WalletBalance.Currency.values().length);
    }

    private User createUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setPassword("{bcrypt}$2a$10$placeholder");
        user.setAccountReference("ACC-" + email.substring(0, 8).toUpperCase().replace("@", "X"));
        return userRepository.save(user);
    }
}
