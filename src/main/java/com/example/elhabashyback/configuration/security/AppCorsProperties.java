package com.example.elhabashyback.configuration.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.security.cors")
public record AppCorsProperties(List<String> allowedOrigins) {
}
