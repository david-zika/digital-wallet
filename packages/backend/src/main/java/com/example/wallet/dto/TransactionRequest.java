package com.example.wallet.dto;

import com.example.wallet.model.Transaction;
import com.example.wallet.model.WalletBalance;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class TransactionRequest {
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;

    @NotNull
    private WalletBalance.Currency currency;

    @NotNull
    private Transaction.Type type;

    private String recipientAccount;
    private String recipientName;
    private String paymentReference;
    private boolean isDemoMode;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public WalletBalance.Currency getCurrency() {
        return currency;
    }

    public void setCurrency(WalletBalance.Currency currency) {
        this.currency = currency;
    }

    public Transaction.Type getType() {
        return type;
    }

    public void setType(Transaction.Type type) {
        this.type = type;
    }

    public String getRecipientAccount() {
        return recipientAccount;
    }

    public void setRecipientAccount(String recipientAccount) {
        this.recipientAccount = recipientAccount;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public void setIsDemoMode(boolean isDemoMode) {
        this.isDemoMode = isDemoMode;
    }

    public boolean isDemoMode() {
        return this.isDemoMode;
    }
}