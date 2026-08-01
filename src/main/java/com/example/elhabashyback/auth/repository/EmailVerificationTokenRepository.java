package com.example.elhabashyback.auth.repository;

import com.example.elhabashyback.auth.entity.EmailVerificationToken;
import com.example.elhabashyback.user.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {
    Optional<EmailVerificationToken> findByTokenHash(String tokenHash);

    Optional<EmailVerificationToken> findFirstByUserOrderByCreatedAtDesc(Users user);

    List<EmailVerificationToken> findAllByUserAndUsedAtIsNull(Users user);
}
