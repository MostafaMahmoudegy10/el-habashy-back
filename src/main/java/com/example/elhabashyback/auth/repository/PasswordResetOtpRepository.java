package com.example.elhabashyback.auth.repository;

import com.example.elhabashyback.auth.entity.PasswordResetOtp;
import com.example.elhabashyback.user.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetOtpRepository extends JpaRepository<PasswordResetOtp, UUID> {
    Optional<PasswordResetOtp> findFirstByUserOrderByCreatedAtDesc(Users user);

    Optional<PasswordResetOtp> findFirstByUserAndUsedAtIsNullOrderByCreatedAtDesc(Users user);

    List<PasswordResetOtp> findAllByUserAndUsedAtIsNull(Users user);
}
