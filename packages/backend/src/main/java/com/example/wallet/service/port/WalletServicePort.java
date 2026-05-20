package com.example.wallet.service.port;

import com.example.wallet.dto.TransactionDTO;
import com.example.wallet.dto.TransactionResponse;
import com.example.wallet.dto.WalletBalanceDTO;
import com.example.wallet.model.Transaction;
import com.example.wallet.model.WalletBalance;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface WalletServicePort {
    List<WalletBalanceDTO> getBalances(UUID userId);
    TransactionResponse getTransactions(UUID userId, int page, int size,
            BigDecimal amountFrom, BigDecimal amountTo, String reference, String type);
    TransactionDTO createTransaction(UUID userId, BigDecimal amount,
            WalletBalance.Currency currency, Transaction.Type type,
            String recipientAccountReference, String recipientName, String paymentReference);
}
