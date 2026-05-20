package com.example.wallet.dto;

import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(max = 255, message = "Full name must not exceed 255 characters")
        String fullName,

        @Size(max = 255, message = "Bank account must not exceed 255 characters")
        String bankAccount
) {
}