package com.example.exception;

import lombok.Getter;

@Getter
public class WalletException extends RuntimeException {
    private final WalletErrorCode errorCode;

    public WalletException(WalletErrorCode errorCode) {
        super(errorCode.name());
        this.errorCode = errorCode;
    }

    public WalletException(WalletErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}