package com.example.elhabashyback.configuration.mail;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.mail")
public record AppMailProperties(
        String from,
        String frontendBaseUrl,
        String backendPublicUrl,
        Duration activationTokenTtl,
        Duration passwordResetOtpTtl,
        Duration resendCooldown
) {
}
