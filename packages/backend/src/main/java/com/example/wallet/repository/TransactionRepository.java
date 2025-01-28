package com.example.wallet.repository;

import com.example.wallet.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    @Query("SELECT t FROM Transaction t " +
            "WHERE t.user.id = :userId " +
            "AND (:type IS NULL OR " +
            "((:type = 'TRANSFER' AND t.recipientAccount LIKE 'ACC-%') OR " +
            "(:type = 'EXTERNAL' AND (t.type = 'DEPOSIT' OR (t.type = 'WITHDRAWAL' AND (t.recipientAccount IS NULL OR t.recipientAccount NOT LIKE 'ACC-%')))))) " +
            "AND (:amountFrom IS NULL OR t.amount >= :amountFrom) " +
            "AND (:amountTo IS NULL OR t.amount <= :amountTo) " +
            "AND ((:reference IS NULL OR :reference = '') OR LOWER(t.paymentReference) LIKE LOWER(CONCAT('%', :reference, '%')) OR LOWER(t.recipientAccount) LIKE LOWER(CONCAT('%', :reference, '%'))) " +
            "ORDER BY t.createdAt DESC")
    Page<Transaction> findTransactions(
            @Param("userId") UUID userId,
            @Param("type") String type,
            @Param("amountFrom") BigDecimal amountFrom,
            @Param("amountTo") BigDecimal amountTo,
            @Param("reference") String reference,
            Pageable pageable
    );
}