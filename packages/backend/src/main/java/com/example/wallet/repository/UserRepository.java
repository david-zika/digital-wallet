package com.example.wallet.repository;

import com.example.wallet.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(@NonNull String email);

    Optional<User> findByAccountReference(@NonNull String accountReference);

    boolean existsByEmail(@NonNull String email);
}