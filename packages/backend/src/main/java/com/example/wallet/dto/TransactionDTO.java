package com.example.wallet.dto;

import com.example.wallet.model.Transaction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDTO {
    private UUID id;
    private BigDecimal amount;
    private String currency;
    private String type;
    private String status;
    private String recipientAccount;
    private String recipientName;
    private String paymentReference;
    private LocalDateTime createdAt;

    public static TransactionDTO fromEntity(Transaction transaction) {
        TransactionDTO dto = new TransactionDTO();
        dto.setId(transaction.getId());
        dto.setAmount(transaction.getAmount());
        dto.setCurrency(transaction.getCurrency().toString());
        dto.setType(transaction.getType().toString());
        dto.setStatus(transaction.getStatus().toString());
        dto.setRecipientAccount(transaction.getRecipientAccount());
        dto.setRecipientName(transaction.getRecipientName());
        dto.setPaymentReference(transaction.getPaymentReference());
        dto.setCreatedAt(transaction.getCreatedAt());
        return dto;
    }
}