package com.example.elhabashyback.auth.service;

import com.example.elhabashyback.auth.entity.EmailVerificationToken;
import com.example.elhabashyback.auth.repository.EmailVerificationTokenRepository;
import com.example.elhabashyback.common.exception.UnauthorizedException;
import com.example.elhabashyback.configuration.mail.AppMailProperties;
import com.example.elhabashyback.mail.ElHabashyMailService;
import com.example.elhabashyback.user.entity.Users;
import com.example.elhabashyback.user.repoistory.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AccountVerificationService {

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final SecureTokenService secureTokenService;
    private final ElHabashyMailService mailService;
    private final AppMailProperties properties;

    @Transactional
    public void sendForNewUser(Users user) {
        issueAndSend(user);
    }

    @Transactional
    public void resend(String requestedEmail) {
        String email = requestedEmail.trim().toLowerCase(Locale.ROOT);
        userRepository.findByEmailIgnoreCase(email).ifPresent(user -> {
            if (Boolean.TRUE.equals(user.getEnabled()) || isCoolingDown(user)) {
                return;
            }
            issueAndSend(user);
        });
    }

    @Transactional
    public void activate(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new UnauthorizedException("Activation token is missing");
        }

        EmailVerificationToken token = tokenRepository.findByTokenHash(secureTokenService.hashToken(rawToken))
                .orElseThrow(() -> new UnauthorizedException("Invalid or expired activation token"));
        Instant now = Instant.now();
        if (!token.isUsable(now)) {
            throw new UnauthorizedException("Invalid or expired activation token");
        }

        token.setUsedAt(now);
        token.getUser().setEnabled(true);
    }

    private void issueAndSend(Users user) {
        Instant now = Instant.now();
        tokenRepository.findAllByUserAndUsedAtIsNull(user).forEach(token -> token.setUsedAt(now));

        String rawToken = secureTokenService.randomToken();
        EmailVerificationToken token = new EmailVerificationToken();
        token.setUser(user);
        token.setTokenHash(secureTokenService.hashToken(rawToken));
        token.setExpiresAt(now.plus(properties.activationTokenTtl()));
        tokenRepository.save(token);
        mailService.sendActivationEmail(user, rawToken);
    }

    private boolean isCoolingDown(Users user) {
        Instant threshold = Instant.now().minus(properties.resendCooldown());
        return tokenRepository.findFirstByUserOrderByCreatedAtDesc(user)
                .map(token -> token.getCreatedAt().isAfter(threshold))
                .orElse(false);
    }
}
