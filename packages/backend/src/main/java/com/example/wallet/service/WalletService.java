package com.example.wallet.service;

import com.example.wallet.model.Transaction;
import com.example.wallet.model.WalletBalance;
import com.example.wallet.model.User;
import com.example.wallet.repository.TransactionRepository;
import com.example.wallet.repository.WalletBalanceRepository;
import com.example.wallet.repository.UserRepository;
import com.example.wallet.dto.TransactionResponse;
import com.example.wallet.dto.TransactionDTO;
import com.example.wallet.exception.WalletException;
import com.example.wallet.exception.WalletErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WalletService {
    private static final BigDecimal MINIMUM_AMOUNT = new BigDecimal("5.00");

    private final WalletBalanceRepository walletBalanceRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<WalletBalance> getBalances(UUID userId) {
        return walletBalanceRepository.findAll().stream()
                .filter(balance -> balance.getUser().getId().equals(userId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TransactionResponse getTransactions(UUID userId, int page, int size,
            BigDecimal amountFrom, BigDecimal amountTo, String reference, String type) {

        Page<Transaction> transactionsPage = transactionRepository.findTransactions(
            userId, type, amountFrom, amountTo, reference, PageRequest.of(page - 1, size)
        );

        return new TransactionResponse(
            transactionsPage.getContent().stream()
                .map(TransactionDTO::fromEntity)
                .collect(Collectors.toList()),
            transactionsPage.getTotalElements(),
            transactionsPage.getTotalPages()
        );
    }

    @Transactional
    public Transaction createTransaction(UUID userId, BigDecimal amount,
            WalletBalance.Currency currency, Transaction.Type type,
            String recipientAccountReference, String recipientName, String paymentReference,
            boolean isDemoMode) {

        validateAmount(amount);
        amount = amount.setScale(2, RoundingMode.HALF_UP);

        User sourceUser = getUser(userId);
        Transaction transaction = createTransactionEntity(sourceUser, amount, currency, type,
                recipientAccountReference, recipientName, paymentReference);

        if (type == Transaction.Type.WITHDRAWAL) {
            handleWithdrawal(transaction, isDemoMode);
        } else if (type == Transaction.Type.DEPOSIT && isDemoMode) {
            handleDemoDeposit(transaction);
        }

        return transactionRepository.save(transaction);
    }

    private void validateAmount(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new WalletException(WalletErrorCode.INVALID_AMOUNT, "Amount must be greater than 0");
        }
        if (amount.compareTo(MINIMUM_AMOUNT) < 0) {
            throw new WalletException(WalletErrorCode.INVALID_AMOUNT, "Minimum amount is " + MINIMUM_AMOUNT);
        }
        if (amount.scale() > 2) {
            throw new WalletException(WalletErrorCode.INVALID_AMOUNT, "Amount cannot have more than 2 decimal places");
        }
    }

    private User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new WalletException(WalletErrorCode.RESOURCE_NOT_FOUND));
    }

    private Transaction getTransaction(UUID transactionId) {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new WalletException(WalletErrorCode.TRANSACTION_NOT_FOUND));
    }

    private WalletBalance getWalletBalance(UUID userId, WalletBalance.Currency currency) {
        return walletBalanceRepository.findByUserIdAndCurrency(userId, currency)
                .orElseThrow(() -> new WalletException(WalletErrorCode.RESOURCE_NOT_FOUND));
    }

    private Transaction createTransactionEntity(User user, BigDecimal amount,
            WalletBalance.Currency currency, Transaction.Type type,
            String recipientAccount, String recipientName, String paymentReference) {

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setAmount(amount);
        transaction.setCurrency(currency);
        transaction.setType(type);
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setRecipientAccount(recipientAccount);
        transaction.setRecipientName(recipientName);
        transaction.setPaymentReference(paymentReference);
        transaction.setStatus(Transaction.Status.PENDING);

        return transaction;
    }

    private void handleWithdrawal(Transaction transaction, boolean isDemoMode) {
        WalletBalance sourceBalance = getWalletBalance(
            transaction.getUser().getId(), transaction.getCurrency()
        );

        if (sourceBalance.getBalance().compareTo(transaction.getAmount()) < 0) {
            throw new WalletException(WalletErrorCode.INSUFFICIENT_FUNDS);
        }

        if (!isDemoMode && transaction.getRecipientAccount() != null
                && transaction.getRecipientAccount().startsWith("ACC-")) {
            handleInternalTransfer(transaction, sourceBalance);
        } else {
            handleExternalWithdrawal(transaction, sourceBalance, isDemoMode);
        }
    }

    private void handleInternalTransfer(Transaction transaction, WalletBalance sourceBalance) {
        User recipientUser = userRepository.findByAccountReference(transaction.getRecipientAccount())
                .orElseThrow(() -> new WalletException(WalletErrorCode.RECIPIENT_NOT_FOUND));

        updateBalance(sourceBalance, transaction.getAmount().negate());
        createRecipientTransaction(transaction, recipientUser);
        transaction.setStatus(Transaction.Status.COMPLETED);
    }

    private void handleExternalWithdrawal(Transaction transaction, WalletBalance sourceBalance, boolean isDemoMode) {
        if (isDemoMode) {
            updateBalance(sourceBalance, transaction.getAmount().negate());
            transaction.setStatus(Transaction.Status.COMPLETED);
        }
    }

    private void handleDemoDeposit(Transaction transaction) {
        WalletBalance balance = getOrCreateWalletBalance(
            transaction.getUser(), transaction.getCurrency()
        );
        updateBalance(balance, transaction.getAmount());
        transaction.setStatus(Transaction.Status.COMPLETED);
    }

    private WalletBalance getOrCreateWalletBalance(User user, WalletBalance.Currency currency) {
        return walletBalanceRepository
                .findByUserIdAndCurrency(user.getId(), currency)
                .orElseGet(() -> {
                    WalletBalance newBalance = new WalletBalance();
                    newBalance.setUser(user);
                    newBalance.setCurrency(currency);
                    newBalance.setBalance(BigDecimal.ZERO);
                    return newBalance;
                });
    }

    private void createRecipientTransaction(Transaction sourceTransaction, User recipientUser) {
        Transaction recipientTransaction = new Transaction();
        recipientTransaction.setUser(recipientUser);
        recipientTransaction.setAmount(sourceTransaction.getAmount());
        recipientTransaction.setCurrency(sourceTransaction.getCurrency());
        recipientTransaction.setType(Transaction.Type.DEPOSIT);
        recipientTransaction.setStatus(Transaction.Status.COMPLETED);
        recipientTransaction.setPaymentReference("Transfer from " + sourceTransaction.getUser().getAccountReference());
        recipientTransaction.setCreatedAt(LocalDateTime.now());
        transactionRepository.save(recipientTransaction);

        WalletBalance recipientBalance = getOrCreateWalletBalance(
            recipientUser, sourceTransaction.getCurrency()
        );
        updateBalance(recipientBalance, sourceTransaction.getAmount());
    }

    private void updateBalance(WalletBalance balance, BigDecimal amount) {
        balance.setBalance(balance.getBalance().add(amount));
        balance.setLastUpdated(LocalDateTime.now());
        walletBalanceRepository.save(balance);
    }

    private void updateBalance(WalletBalance balance, Transaction transaction) {
        BigDecimal amount = transaction.getType() == Transaction.Type.DEPOSIT ?
                transaction.getAmount() : transaction.getAmount().negate();
        updateBalance(balance, amount);
    }

    private void completeTransaction(Transaction transaction) {
        transaction.setStatus(Transaction.Status.COMPLETED);
        transactionRepository.save(transaction);
    }

    private void failTransaction(Transaction transaction) {
        transaction.setStatus(Transaction.Status.FAILED);
        transactionRepository.save(transaction);
    }
}