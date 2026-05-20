package com.example.wallet.controller;

import com.example.wallet.dto.TransactionDTO;
import com.example.wallet.dto.TransactionRequest;
import com.example.wallet.dto.TransactionResponse;
import com.example.wallet.dto.WalletBalanceDTO;
import com.example.wallet.security.UserPrincipal;
import com.example.wallet.service.port.WalletServicePort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
@Validated
@Tag(name = "Wallet", description = "Wallet management API")
public class WalletController {

    private final WalletServicePort walletService;

    @Operation(summary = "Get wallet balances",
               description = "Retrieves current balances for all currencies in the user's wallet")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Balances retrieved successfully",
            content = @Content(schema = @Schema(implementation = WalletBalanceDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/balances")
    public ResponseEntity<List<WalletBalanceDTO>> getBalances(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(walletService.getBalances(principal.getId()));
    }

    @Operation(summary = "Get transactions",
               description = "Retrieves paginated list of transactions with optional filters")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully",
            content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/transactions")
    public ResponseEntity<TransactionResponse> getTransactions(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "Page number (1-based, min 1)")
                @RequestParam(defaultValue = "1") @Min(1) int page,
            @Parameter(description = "Page size (max 100)")
                @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @Parameter(description = "Minimum amount")
                @RequestParam(required = false) BigDecimal amountFrom,
            @Parameter(description = "Maximum amount")
                @RequestParam(required = false) BigDecimal amountTo,
            @Parameter(description = "Payment reference filter")
                @RequestParam(required = false) String reference,
            @Parameter(description = "Transaction type (TRANSFER/EXTERNAL)")
                @RequestParam(required = false) String type) {

        return ResponseEntity.ok(walletService.getTransactions(
                principal.getId(), page, size, amountFrom, amountTo, reference, type));
    }

    @Operation(summary = "Create transaction",
               description = "Creates a new transaction (deposit, withdrawal, or transfer)")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Transaction created successfully",
            content = @Content(schema = @Schema(implementation = TransactionDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "402", description = "Insufficient funds"),
        @ApiResponse(responseCode = "404", description = "Recipient not found")
    })
    @PostMapping("/transactions")
    public ResponseEntity<TransactionDTO> createTransaction(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody TransactionRequest request) {

        TransactionDTO transaction = walletService.createTransaction(
                principal.getId(),
                request.amount(),
                request.currency(),
                request.type(),
                request.recipientAccount(),
                request.recipientName(),
                request.paymentReference()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }
}