package com.example.wallet.repository;

import com.example.wallet.model.WalletBalance;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WalletBalanceRepository extends JpaRepository<WalletBalance, Long> {
    Optional<WalletBalance> findByUserIdAndCurrency(@NonNull UUID userId, @NonNull WalletBalance.Currency currency);

    List<WalletBalance> findByUserId(@NonNull UUID userId);

    /**
     * Fetches the balance with a pessimistic write lock (SELECT … FOR UPDATE).
     * Must only be called within a @Transactional method that deducts funds.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT wb FROM WalletBalance wb WHERE wb.user.id = :userId AND wb.currency = :currency")
    Optional<WalletBalance> findByUserIdAndCurrencyWithLock(
            @Param("userId") @NonNull UUID userId,
            @Param("currency") @NonNull WalletBalance.Currency currency);
}