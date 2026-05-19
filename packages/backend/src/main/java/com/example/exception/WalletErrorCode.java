package com.example.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WalletErrorCode {
    // Obecné chyby (1xxx)
    INVALID_REQUEST("1001"),
    UNAUTHORIZED("1002"),
    RESOURCE_NOT_FOUND("1003"),

    // Autentizační chyby (2xxx)
    INVALID_CREDENTIALS("2001"),
    EMAIL_ALREADY_EXISTS("2002"),
    INVALID_PASSWORD("2003"),

    // Transakční chyby (3xxx)
    INSUFFICIENT_FUNDS("3001"),
    INVALID_AMOUNT("3002"),
    INVALID_CURRENCY("3003"),
    RECIPIENT_NOT_FOUND("3004"),
    TRANSACTION_FAILED("3005"),
    TRANSACTION_NOT_FOUND("3006"),

    // Validační chyby (4xxx)
    MISSING_REQUIRED_FIELD("4001"),
    INVALID_FIELD_FORMAT("4002"),
    INVALID_FIELD_VALUE("4003");

    private final String code;
}

