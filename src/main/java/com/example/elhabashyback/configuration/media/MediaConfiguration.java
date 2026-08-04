package com.example.elhabashyback.configuration.media;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(CloudinaryProperties.class)
@EnableAsync
public class MediaConfiguration {

    @Bean
    RestClient cloudinaryRestClient() {
        return RestClient.create();
    }

    @Bean(name = "mediaUploadExecutor")
    ThreadPoolTaskExecutor mediaUploadExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(20);
        executor.setThreadNamePrefix("media-upload-");
        return executor;
    }
}
