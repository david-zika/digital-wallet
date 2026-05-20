package com.example.wallet.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WalletErrorCode {
    // General errors (1xxx)
    INVALID_REQUEST("1001", "Invalid request"),
    UNAUTHORIZED("1002", "Unauthorized"),
    RESOURCE_NOT_FOUND("1003", "Resource not found"),
    // Authentication errors (2xxx)
    INVALID_CREDENTIALS("2001", "Invalid email or password"),
    EMAIL_ALREADY_EXISTS("2002", "An account with this email already exists"),
    INVALID_PASSWORD("2003", "Invalid password"),
    // Transaction errors (3xxx)
    INSUFFICIENT_FUNDS("3001", "Insufficient funds"),
    INVALID_AMOUNT("3002", "Invalid amount"),
    INVALID_CURRENCY("3003", "Invalid or unsupported currency"),
    RECIPIENT_NOT_FOUND("3004", "Recipient account not found"),
    TRANSACTION_FAILED("3005", "Transaction failed"),
    TRANSACTION_NOT_FOUND("3006", "Transaction not found"),
    // Validation errors (4xxx)
    MISSING_REQUIRED_FIELD("4001", "Required field is missing"),
    INVALID_FIELD_FORMAT("4002", "Field has invalid format"),
    INVALID_FIELD_VALUE("4003", "Field has invalid value");

    private final String code;
    private final String defaultMessage;
}
