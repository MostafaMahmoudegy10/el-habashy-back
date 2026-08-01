package com.example.elhabashyback.configuration.flyway;

import com.example.elhabashyback.configuration.security.AppJwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"prod"})
public class FlywayConfig {

    @Bean
    public FlywayMigrationStrategy flyway(){
        return  flyway -> {
            flyway.clean();
            flyway.migrate();
        };
    }
}
