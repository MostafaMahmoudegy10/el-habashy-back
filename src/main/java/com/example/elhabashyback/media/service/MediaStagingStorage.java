package com.example.elhabashyback.media.service;

import com.example.elhabashyback.media.exception.MediaUploadException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class MediaStagingStorage {

    private final Path root;

    public MediaStagingStorage(@Value("${media.upload.temp-dir:./data/media-temp}") String tempDir) {
        this.root = Path.of(tempDir).toAbsolutePath().normalize();
    }

    public Path stage(MultipartFile file, Long mediaId) {
        Path target = null;
        try {
            Files.createDirectories(root);
            target = root.resolve(mediaId + "-" + UUID.randomUUID() + extension(file.getOriginalFilename()))
                    .normalize();
            requireInsideRoot(target);
            try (var input = file.getInputStream()) {
                Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return target;
        } catch (IOException exception) {
            if (target != null) {
                try {
                    Files.deleteIfExists(target);
                } catch (IOException ignored) {
                    // The staging exception remains the primary failure.
                }
            }
            throw new MediaUploadException("Could not stage the media file for background upload", exception);
        }
    }

    public void delete(Path path) {
        if (path == null) {
            return;
        }
        Path target = path.toAbsolutePath().normalize();
        requireInsideRoot(target);
        try {
            Files.deleteIfExists(target);
        } catch (IOException exception) {
            throw new MediaUploadException("Could not delete the staged video", exception);
        }
    }

    private String extension(String fileName) {
        if (fileName == null) {
            return "";
        }
        int separator = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
        int dot = fileName.lastIndexOf('.');
        if (dot <= separator || dot < 0 || fileName.length() - dot > 10) {
            return "";
        }
        return fileName.substring(dot).toLowerCase();
    }

    private void requireInsideRoot(Path target) {
        if (!target.startsWith(root)) {
            throw new MediaUploadException("Invalid media staging path");
        }
    }
}
