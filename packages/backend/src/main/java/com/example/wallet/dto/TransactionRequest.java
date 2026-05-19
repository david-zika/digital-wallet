package com.example.wallet.dto;

import com.example.wallet.model.Transaction;
import com.example.wallet.model.WalletBalance;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransactionRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "5.00", message = "Minimum transaction amount is 5.00")
    private BigDecimal amount;

    @NotNull(message = "Currency is required")
    private WalletBalance.Currency currency;

    @NotNull(message = "Transaction type is required")
    private Transaction.Type type;

    private String recipientAccount;
    private String recipientName;
    private String paymentReference;
}