package com.example.elhabashyback.auth.service;

import com.example.elhabashyback.auth.dto.ResetPasswordRequest;
import com.example.elhabashyback.auth.entity.PasswordResetOtp;
import com.example.elhabashyback.auth.repository.PasswordResetOtpRepository;
import com.example.elhabashyback.auth.repository.RefreshTokenRepository;
import com.example.elhabashyback.common.exception.UnauthorizedException;
import com.example.elhabashyback.configuration.mail.AppMailProperties;
import com.example.elhabashyback.mail.ElHabashyMailService;
import com.example.elhabashyback.user.entity.Users;
import com.example.elhabashyback.user.repoistory.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetOtpRepository otpRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final SecureTokenService secureTokenService;
    private final ElHabashyMailService mailService;
    private final PasswordEncoder passwordEncoder;
    private final AppMailProperties properties;

    @Transactional
    public void requestReset(String requestedEmail) {
        String email = normalizeEmail(requestedEmail);
        userRepository.findByEmailIgnoreCase(email).ifPresent(user -> {
            if (!Boolean.TRUE.equals(user.getEnabled()) || isCoolingDown(user)) {
                return;
            }
            issueAndSend(user);
        });
    }

    @Transactional(noRollbackFor = UnauthorizedException.class)
    public void reset(ResetPasswordRequest request) {
        String email = normalizeEmail(request.email());
        Users user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid or expired OTP"));
        PasswordResetOtp storedOtp = otpRepository
                .findFirstByUserAndUsedAtIsNullOrderByCreatedAtDesc(user)
                .orElseThrow(() -> new UnauthorizedException("Invalid or expired OTP"));

        Instant now = Instant.now();
        String providedHash = secureTokenService.hashOtp(email, request.otp());
        if (!storedOtp.isUsable(now) || !secureTokenService.matches(storedOtp.getOtpHash(), providedHash)) {
            storedOtp.setFailedAttempts(storedOtp.getFailedAttempts() + 1);
            if (storedOtp.getFailedAttempts() >= 5 || !storedOtp.getExpiresAt().isAfter(now)) {
                storedOtp.setUsedAt(now);
            }
            throw new UnauthorizedException("Invalid or expired OTP");
        }

        storedOtp.setUsedAt(now);
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        refreshTokenRepository.findAllByUserAndRevokedAtIsNull(user)
                .forEach(token -> token.setRevokedAt(now));
    }

    private void issueAndSend(Users user) {
        Instant now = Instant.now();
        otpRepository.findAllByUserAndUsedAtIsNull(user).forEach(otp -> otp.setUsedAt(now));

        String otpValue = secureTokenService.numericOtp();
        PasswordResetOtp otp = new PasswordResetOtp();
        otp.setUser(user);
        otp.setOtpHash(secureTokenService.hashOtp(user.getEmail(), otpValue));
        otp.setExpiresAt(now.plus(properties.passwordResetOtpTtl()));
        otpRepository.save(otp);
        mailService.sendPasswordResetOtp(user, otpValue);
    }

    private boolean isCoolingDown(Users user) {
        Instant threshold = Instant.now().minus(properties.resendCooldown());
        return otpRepository.findFirstByUserOrderByCreatedAtDesc(user)
                .map(otp -> otp.getCreatedAt().isAfter(threshold))
                .orElse(false);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
