package com.example.elhabashyback.configuration.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.security.jwt")
public record AppJwtProperties(
        String issuer,
        String audience,
        String secret,
        Duration accessTokenTtl,
        Duration refreshTokenTtl,
        String refreshCookieName,
        boolean refreshCookieSecure,
        String refreshCookieSameSite
){
}
