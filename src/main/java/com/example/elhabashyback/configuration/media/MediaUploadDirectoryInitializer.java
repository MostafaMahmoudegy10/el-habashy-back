package com.example.elhabashyback.configuration.media;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class MediaUploadDirectoryInitializer {

    private final Path incomingDirectory;
    private final Path stagingDirectory;

    public MediaUploadDirectoryInitializer(
            @Value("${spring.servlet.multipart.location:./data/incoming-media}") String incomingDirectory,
            @Value("${media.upload.temp-dir:./data/media-temp}") String stagingDirectory
    ) {
        this.incomingDirectory = Path.of(incomingDirectory).toAbsolutePath().normalize();
        this.stagingDirectory = Path.of(stagingDirectory).toAbsolutePath().normalize();
    }

    @PostConstruct
    void createDirectories() {
        try {
            Files.createDirectories(incomingDirectory);
            Files.createDirectories(stagingDirectory);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not create media upload directories", exception);
        }
    }
}
