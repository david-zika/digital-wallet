package com.example.wallet.repository;

import com.example.wallet.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    Optional<User> findByAccountReference(String accountReference);

    boolean existsByEmail(String email);
}