package com.example.wallet.dto;

import java.util.List;

public record TransactionResponse(
        List<TransactionDTO> transactions,
        long total,
        int totalPages
) {
}