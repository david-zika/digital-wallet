package com.example.wallet.dto;

import com.example.wallet.model.WalletBalance;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WalletBalanceDTO {
    private String currency;
    private BigDecimal balance;
    private LocalDateTime lastUpdated;

    public static WalletBalanceDTO fromEntity(WalletBalance walletBalance) {
        return new WalletBalanceDTO(
                walletBalance.getCurrency().name(),
                walletBalance.getBalance(),
                walletBalance.getLastUpdated()
        );
    }
}

