package com.example.elhabashyback.configuration.security;

import com.nimbusds.jose.jwk.source.JWKSource;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.spec.SecretKeySpec;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(AppJwtProperties.class)
public class JWTConfiguration {

    private final AppJwtProperties appJwtProperties;

    @Bean
    public JwtEncoder  jwtEncoder() {
        SecretKeySpec secretKeySpec= new SecretKeySpec(appJwtProperties.secret().getBytes(),"HmacSHA256"); // he takes teh bytes of the secret and the algorithm
        return NimbusJwtEncoder.withSecretKey(secretKeySpec).build();
    }
}
