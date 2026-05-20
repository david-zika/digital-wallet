package com.example.wallet.dto;

import com.example.wallet.model.Transaction;
import com.example.wallet.model.WalletBalance;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransactionRequest(
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "5.00", message = "Minimum transaction amount is 5.00")
        BigDecimal amount,

        @NotNull(message = "Currency is required")
        WalletBalance.Currency currency,

        @NotNull(message = "Transaction type is required")
        Transaction.Type type,

        String recipientAccount,
        String recipientName,
        String paymentReference
) {
}