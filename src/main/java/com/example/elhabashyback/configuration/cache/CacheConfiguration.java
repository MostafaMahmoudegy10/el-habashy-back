package com.example.elhabashyback.configuration.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfiguration {

    public static final String PUBLIC_LISTINGS = "public-listings";
    public static final String PUBLIC_ABOUT = "public-about";
    public static final String PUBLIC_SECTORS = "public-sectors";
    public static final String PUBLIC_SETTINGS = "public-settings";

    @Bean
    CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager(
                PUBLIC_LISTINGS,
                PUBLIC_ABOUT,
                PUBLIC_SECTORS,
                PUBLIC_SETTINGS
        );
        manager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(Duration.ofSeconds(45)));
        return manager;
    }
}
