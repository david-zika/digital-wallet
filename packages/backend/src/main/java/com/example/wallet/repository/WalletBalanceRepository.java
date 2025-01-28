package com.example.wallet.repository;

import com.example.wallet.model.WalletBalance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WalletBalanceRepository extends JpaRepository<WalletBalance, Long> {
    Optional<WalletBalance> findByUserIdAndCurrency(UUID userId, WalletBalance.Currency currency);
}