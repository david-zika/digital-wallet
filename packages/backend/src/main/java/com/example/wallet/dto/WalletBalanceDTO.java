package com.example.wallet.dto;

import com.example.wallet.model.WalletBalance;

import java.math.BigDecimal;
import java.time.Instant;

public record WalletBalanceDTO(
        String currency,
        BigDecimal balance,
        Instant lastUpdated
) {
    public static WalletBalanceDTO fromEntity(WalletBalance walletBalance) {
        return new WalletBalanceDTO(
                walletBalance.getCurrency().name(),
                walletBalance.getBalance(),
                walletBalance.getLastUpdated()
        );
    }
}

