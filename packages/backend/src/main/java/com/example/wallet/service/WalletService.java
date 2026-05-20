package com.example.wallet.service;

import com.example.wallet.config.AppProperties;
import com.example.wallet.dto.TransactionDTO;
import com.example.wallet.dto.TransactionResponse;
import com.example.wallet.dto.WalletBalanceDTO;
import com.example.wallet.exception.WalletErrorCode;
import com.example.wallet.exception.WalletException;
import com.example.wallet.model.Transaction;
import com.example.wallet.model.User;
import com.example.wallet.model.WalletBalance;
import com.example.wallet.repository.TransactionRepository;
import com.example.wallet.repository.UserRepository;
import com.example.wallet.repository.WalletBalanceRepository;
import com.example.wallet.service.port.WalletServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class WalletService implements WalletServicePort {

    private static final BigDecimal MINIMUM_AMOUNT = new BigDecimal("5.00");
    private static final Pattern INTERNAL_ACCOUNT_PATTERN = Pattern.compile("^ACC-[A-F0-9]{8}$");

    private final WalletBalanceRepository walletBalanceRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final AppProperties appProperties;

    @Override
    @Transactional(readOnly = true)
    public List<WalletBalanceDTO> getBalances(UUID userId) {
        return walletBalanceRepository.findByUserId(userId).stream()
                .map(WalletBalanceDTO::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransactions(UUID userId, int page, int size,
            BigDecimal amountFrom, BigDecimal amountTo, String reference, String type) {
        Page<Transaction> transactionsPage = transactionRepository.findTransactions(
                userId, type, amountFrom, amountTo, reference, PageRequest.of(page - 1, size));
        return new TransactionResponse(
                transactionsPage.getContent().stream()
                        .map(TransactionDTO::fromEntity)
                        .toList(),
                transactionsPage.getTotalElements(),
                transactionsPage.getTotalPages()
        );
    }

    @Override
    @Transactional
    public TransactionDTO createTransaction(UUID userId, BigDecimal amount,
            WalletBalance.Currency currency, Transaction.Type type,
            String recipientAccountReference, String recipientName, String paymentReference) {
        validateAmount(amount);
        amount = amount.setScale(2, RoundingMode.HALF_UP);

        User sourceUser = getUser(userId);
        Transaction transaction = buildTransactionEntity(sourceUser, amount, currency, type,
                recipientAccountReference, recipientName, paymentReference);

        if (type == Transaction.Type.WITHDRAWAL) {
            handleWithdrawal(transaction);
        } else if (type == Transaction.Type.DEPOSIT) {
            handleDeposit(transaction);
        }

        return TransactionDTO.fromEntity(transactionRepository.save(transaction));
    }

    private void validateAmount(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new WalletException(WalletErrorCode.INVALID_AMOUNT, "Amount must be greater than 0");
        }
        if (amount.compareTo(MINIMUM_AMOUNT) < 0) {
            throw new WalletException(WalletErrorCode.INVALID_AMOUNT,
                    "Minimum amount is " + MINIMUM_AMOUNT);
        }
        if (amount.stripTrailingZeros().scale() > 2) {
            throw new WalletException(WalletErrorCode.INVALID_AMOUNT,
                    "Amount cannot have more than 2 decimal places");
        }
    }

    private User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new WalletException(WalletErrorCode.RESOURCE_NOT_FOUND));
    }

    private WalletBalance getWalletBalanceWithLock(UUID userId, WalletBalance.Currency currency) {
        return walletBalanceRepository.findByUserIdAndCurrencyWithLock(userId, currency)
                .orElseThrow(() -> new WalletException(WalletErrorCode.RESOURCE_NOT_FOUND,
                        "No balance found for currency " + currency));
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

    private Transaction buildTransactionEntity(User user, BigDecimal amount,
            WalletBalance.Currency currency, Transaction.Type type,
            String recipientAccount, String recipientName, String paymentReference) {
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setAmount(amount);
        transaction.setCurrency(currency);
        transaction.setType(type);
        transaction.setRecipientAccount(recipientAccount);
        transaction.setRecipientName(recipientName);
        transaction.setPaymentReference(paymentReference);
        transaction.setStatus(Transaction.Status.PENDING);
        return transaction;
    }

    private void handleWithdrawal(Transaction transaction) {
        WalletBalance sourceBalance = getWalletBalanceWithLock(
                transaction.getUser().getId(), transaction.getCurrency());

        if (sourceBalance.getBalance().compareTo(transaction.getAmount()) < 0) {
            throw new WalletException(WalletErrorCode.INSUFFICIENT_FUNDS);
        }

        String recipient = transaction.getRecipientAccount();
        if (recipient != null && INTERNAL_ACCOUNT_PATTERN.matcher(recipient).matches()) {
            handleInternalTransfer(transaction, sourceBalance);
        } else {
            handleExternalWithdrawal(transaction, sourceBalance);
        }
    }

    private void handleInternalTransfer(Transaction transaction, WalletBalance sourceBalance) {
        User recipientUser = userRepository.findByAccountReference(transaction.getRecipientAccount())
                .orElseThrow(() -> new WalletException(WalletErrorCode.RECIPIENT_NOT_FOUND));
        updateBalance(sourceBalance, transaction.getAmount().negate());
        createRecipientDepositTransaction(transaction, recipientUser);
        transaction.setStatus(Transaction.Status.COMPLETED);
    }

    private void handleExternalWithdrawal(Transaction transaction, WalletBalance sourceBalance) {
        updateBalance(sourceBalance, transaction.getAmount().negate());
        transaction.setStatus(appProperties.demoMode()
                ? Transaction.Status.COMPLETED
                : Transaction.Status.PENDING);
    }

    private void handleDeposit(Transaction transaction) {
        if (appProperties.demoMode()) {
            WalletBalance balance = getOrCreateWalletBalance(
                    transaction.getUser(), transaction.getCurrency());
            updateBalance(balance, transaction.getAmount());
            transaction.setStatus(Transaction.Status.COMPLETED);
        } else {
            transaction.setStatus(Transaction.Status.PENDING);
        }
    }

    private void createRecipientDepositTransaction(Transaction sourceTransaction, User recipientUser) {
        Transaction recipientTransaction = new Transaction();
        recipientTransaction.setUser(recipientUser);
        recipientTransaction.setAmount(sourceTransaction.getAmount());
        recipientTransaction.setCurrency(sourceTransaction.getCurrency());
        recipientTransaction.setType(Transaction.Type.DEPOSIT);
        recipientTransaction.setStatus(Transaction.Status.COMPLETED);
        recipientTransaction.setPaymentReference(
                "Transfer from " + sourceTransaction.getUser().getAccountReference());
        transactionRepository.save(recipientTransaction);

        WalletBalance recipientBalance = getOrCreateWalletBalance(
                recipientUser, sourceTransaction.getCurrency());
        updateBalance(recipientBalance, sourceTransaction.getAmount());
    }

    private void updateBalance(WalletBalance balance, BigDecimal delta) {
        balance.setBalance(balance.getBalance().add(delta));
        walletBalanceRepository.save(balance);
    }
}