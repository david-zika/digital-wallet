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

public interface WalletBalanceRepository extends JpaRepository<WalletBalance, UUID> {
    Optional<WalletBalance> findByUserIdAndCurrency(@NonNull UUID userId, @NonNull WalletBalance.Currency currency);

    List<WalletBalance> findByUserId(@NonNull UUID userId);

    /**
     * Acquires a pessimistic write lock (SELECT … FOR UPDATE) on the balance row.
     * Must only be called within a {@code @Transactional} method that deducts funds to prevent double-spend.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT wb FROM WalletBalance wb WHERE wb.user.id = :userId AND wb.currency = :currency")
    Optional<WalletBalance> findByUserIdAndCurrencyWithLock(
            @Param("userId") @NonNull UUID userId,
            @Param("currency") @NonNull WalletBalance.Currency currency);
}