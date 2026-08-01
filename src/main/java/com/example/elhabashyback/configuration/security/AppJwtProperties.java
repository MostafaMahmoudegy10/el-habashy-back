package com.example.elhabashyback.configuration.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security.jwt")
public record AppJwtProperties(
        String issuer,
        String audience,
        String secret
){
}
