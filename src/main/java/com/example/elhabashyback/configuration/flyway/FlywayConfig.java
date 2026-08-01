package com.example.elhabashyback.configuration.flyway;

import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"clean"})
public class FlywayConfig {

    @Bean
    public FlywayMigrationStrategy cleanMigrateStrategy(){
        return  flyway -> {
            flyway.clean();
            flyway.migrate();
        };
    }
}
