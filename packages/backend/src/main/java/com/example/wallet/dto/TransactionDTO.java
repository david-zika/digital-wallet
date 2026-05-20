package com.example.wallet.dto;

import com.example.wallet.model.Transaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionDTO(
        UUID id,
        BigDecimal amount,
        String currency,
        String type,
        String status,
        String recipientAccount,
        String recipientName,
        String paymentReference,
        Instant createdAt
) {
    public static TransactionDTO fromEntity(Transaction transaction) {
        return new TransactionDTO(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getCurrency().toString(),
                transaction.getType().toString(),
                transaction.getStatus().toString(),
                transaction.getRecipientAccount(),
                transaction.getRecipientName(),
                transaction.getPaymentReference(),
                transaction.getCreatedAt()
        );
    }
}