package com.example.wallet.dto;

public record ProfileResponse(
        String email,
        String accountReference,
        String fullName,
        String bankAccount
) {
}