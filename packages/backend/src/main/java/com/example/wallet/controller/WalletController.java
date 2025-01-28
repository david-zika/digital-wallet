package com.example.wallet.controller;

import com.example.wallet.model.Transaction;
import com.example.wallet.model.WalletBalance;
import com.example.wallet.service.WalletService;
import com.example.wallet.dto.TransactionRequest;
import com.example.wallet.dto.TransactionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
@Validated
@Tag(name = "Wallet", description = "Wallet management API")
public class WalletController {
    private final WalletService walletService;

    @Operation(
        summary = "Get wallet balances",
        description = "Retrieves current balances for all currencies in the user's wallet"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Balances retrieved successfully",
            content = @Content(schema = @Schema(implementation = WalletBalance.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/balances")
    public ResponseEntity<List<WalletBalance>> getBalances(
            @AuthenticationPrincipal com.example.wallet.model.User userDetails) {
        return ResponseEntity.ok(walletService.getBalances(userDetails.getId()));
    }

    @Operation(
        summary = "Get transactions",
        description = "Retrieves paginated list of transactions with optional filters"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully",
            content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/transactions")
    public ResponseEntity<TransactionResponse> getTransactions(
            @AuthenticationPrincipal com.example.wallet.model.User userDetails,
            @Parameter(description = "Page number (1-based)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Minimum amount") @RequestParam(required = false) BigDecimal amountFrom,
            @Parameter(description = "Maximum amount") @RequestParam(required = false) BigDecimal amountTo,
            @Parameter(description = "Payment reference") @RequestParam(required = false) String reference,
            @Parameter(description = "Transaction type (TRANSFER/EXTERNAL)") @RequestParam(required = false) String type) {

        return ResponseEntity.ok(walletService.getTransactions(
            userDetails.getId(),
            page,
            size,
            amountFrom,
            amountTo,
            reference,
            type
        ));
    }

    @Operation(
        summary = "Create transaction",
        description = "Creates a new transaction (deposit, withdrawal, or transfer)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Transaction created successfully",
            content = @Content(schema = @Schema(implementation = Transaction.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "402", description = "Insufficient funds"),
        @ApiResponse(responseCode = "404", description = "Recipient not found")
    })
    @PostMapping("/transactions")
    public ResponseEntity<Transaction> createTransaction(
            @AuthenticationPrincipal com.example.wallet.model.User userDetails,
            @Valid @RequestBody TransactionRequest request) {

        Transaction transaction = walletService.createTransaction(
                userDetails.getId(),
                request.getAmount(),
                request.getCurrency(),
                request.getType(),
                request.getRecipientAccount(),
                request.getRecipientName(),
                request.getPaymentReference(),
                request.isDemoMode()
        );

        return ResponseEntity.ok(transaction);
    }
}