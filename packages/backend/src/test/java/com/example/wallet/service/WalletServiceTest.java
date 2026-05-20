package com.example.wallet.service;

import com.example.wallet.config.AppProperties;
import com.example.wallet.dto.TransactionDTO;
import com.example.wallet.dto.TransactionResponse;
import com.example.wallet.exception.WalletException;
import com.example.wallet.model.Transaction;
import com.example.wallet.model.User;
import com.example.wallet.model.WalletBalance;
import com.example.wallet.repository.TransactionRepository;
import com.example.wallet.repository.UserRepository;
import com.example.wallet.repository.WalletBalanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock private WalletBalanceRepository walletBalanceRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private UserRepository userRepository;
    @Mock private AppProperties appProperties;

    @InjectMocks
    private WalletService walletService;

    private User testUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testUser = new User();
        testUser.setId(userId);
        testUser.setEmail("user@example.com");
        testUser.setAccountReference("ACC-ABCD1234");
    }

    @Test
    void getBalances_shouldReturnMappedDTOs() {
        WalletBalance balance = new WalletBalance();
        balance.setId(UUID.randomUUID());
        balance.setUser(testUser);
        balance.setCurrency(WalletBalance.Currency.EUR);
        balance.setBalance(new BigDecimal("100.00"));
        when(walletBalanceRepository.findByUserId(userId)).thenReturn(List.of(balance));

        var result = walletService.getBalances(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).currency()).isEqualTo("EUR");
        assertThat(result.get(0).balance()).isEqualByComparingTo("100.00");
    }

    @Test
    void getTransactions_shouldReturnPaginatedResults() {
        Transaction tx = buildTransaction(Transaction.Type.DEPOSIT, new BigDecimal("50.00"));
        when(transactionRepository.findTransactions(eq(userId), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(tx)));

        TransactionResponse result = walletService.getTransactions(userId, 1, 10, null, null, null, null);

        assertThat(result.transactions()).hasSize(1);
        assertThat(result.total()).isEqualTo(1);
    }

    @Test
    void createDepositTransaction_demoMode_shouldCreditBalance() {
        when(appProperties.demoMode()).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        WalletBalance balance = existingBalance(WalletBalance.Currency.EUR, "0.00");
        when(walletBalanceRepository.findByUserIdAndCurrency(userId, WalletBalance.Currency.EUR))
                .thenReturn(Optional.of(balance));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            t.setCreatedAt(Instant.now());
            return t;
        });

        TransactionDTO result = walletService.createTransaction(
                userId, new BigDecimal("50.00"), WalletBalance.Currency.EUR,
                Transaction.Type.DEPOSIT, null, null, null);

        assertThat(result.status()).isEqualTo("COMPLETED");
        assertThat(balance.getBalance()).isEqualByComparingTo("50.00");
    }

    @Test
    void createWithdrawal_shouldDeductBalance() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        WalletBalance balance = existingBalance(WalletBalance.Currency.CZK, "200.00");
        when(walletBalanceRepository.findByUserIdAndCurrencyWithLock(userId, WalletBalance.Currency.CZK))
                .thenReturn(Optional.of(balance));
        when(appProperties.demoMode()).thenReturn(false);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            t.setCreatedAt(Instant.now());
            return t;
        });

        walletService.createTransaction(
                userId, new BigDecimal("50.00"), WalletBalance.Currency.CZK,
                Transaction.Type.WITHDRAWAL, "external-bank-account", null, null);

        assertThat(balance.getBalance()).isEqualByComparingTo("150.00");
    }

    @Test
    void createWithdrawal_insufficientFunds_shouldThrow() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        WalletBalance balance = existingBalance(WalletBalance.Currency.EUR, "10.00");
        when(walletBalanceRepository.findByUserIdAndCurrencyWithLock(userId, WalletBalance.Currency.EUR))
                .thenReturn(Optional.of(balance));

        assertThatThrownBy(() -> walletService.createTransaction(
                userId, new BigDecimal("50.00"), WalletBalance.Currency.EUR,
                Transaction.Type.WITHDRAWAL, null, null, null))
                .isInstanceOf(WalletException.class);
    }

    @Test
    void createTransaction_invalidAmount_shouldThrow() {
        assertThatThrownBy(() -> walletService.createTransaction(
                userId, new BigDecimal("-10.00"), WalletBalance.Currency.EUR,
                Transaction.Type.DEPOSIT, null, null, null))
                .isInstanceOf(WalletException.class);
    }

    @Test
    void createTransaction_belowMinimum_shouldThrow() {
        assertThatThrownBy(() -> walletService.createTransaction(
                userId, new BigDecimal("1.00"), WalletBalance.Currency.EUR,
                Transaction.Type.DEPOSIT, null, null, null))
                .isInstanceOf(WalletException.class);
    }

    // -------------------------------------------------------------------------

    private WalletBalance existingBalance(WalletBalance.Currency currency, String amount) {
        WalletBalance wb = new WalletBalance();
        wb.setId(UUID.randomUUID());
        wb.setUser(testUser);
        wb.setCurrency(currency);
        wb.setBalance(new BigDecimal(amount));
        return wb;
    }

    private Transaction buildTransaction(Transaction.Type type, BigDecimal amount) {
        Transaction tx = new Transaction();
        tx.setId(UUID.randomUUID());
        tx.setUser(testUser);
        tx.setType(type);
        tx.setAmount(amount);
        tx.setCurrency(WalletBalance.Currency.EUR);
        tx.setStatus(Transaction.Status.COMPLETED);
        tx.setCreatedAt(Instant.now());
        return tx;
    }
}
