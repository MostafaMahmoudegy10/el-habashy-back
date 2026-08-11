package com.example.elhabashyback.about.service;

import com.example.elhabashyback.about.dto.AboutImageResponse;
import com.example.elhabashyback.common.exception.BadRequestException;
import com.example.elhabashyback.configuration.media.CloudinaryProperties;
import com.example.elhabashyback.media.service.CloudinaryUploadClient;
import com.example.elhabashyback.media.service.MediaStagingStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AboutImageService {

    private static final long MAX_IMAGE_BYTES = 10L * 1024L * 1024L;
    private static final Set<String> IMAGE_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );
    private static final Map<String, String> CONTENT_TYPES_BY_EXTENSION = Map.of(
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "png", "image/png",
            "webp", "image/webp",
            "gif", "image/gif"
    );

    private final CloudinaryUploadClient cloudinaryUploadClient;
    private final MediaStagingStorage stagingStorage;
    private final CloudinaryProperties properties;

    public AboutImageResponse upload(MultipartFile file) {
        cloudinaryUploadClient.ensureConfigured();
        String fileName = fileName(file);
        String contentType = contentType(file, fileName);
        validate(file, contentType);

        Path stagedFile = stagingStorage.stage(file, Instant.now().toEpochMilli());
        try {
            String configuredFolder = properties.folder();
            String baseFolder = configuredFolder == null || configuredFolder.isBlank()
                    ? "el-habashy"
                    : configuredFolder.trim().replaceAll("/+$", "");
            String publicId = baseFolder + "/about/" + UUID.randomUUID();
            return AboutImageResponse.from(cloudinaryUploadClient.uploadImage(
                    stagedFile,
                    fileName,
                    publicId,
                    contentType,
                    file.getSize()
            ));
        } finally {
            stagingStorage.delete(stagedFile);
        }
    }

    private void validate(MultipartFile file, String contentType) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Image file is required and cannot be empty");
        }
        if (!IMAGE_CONTENT_TYPES.contains(contentType)) {
            throw new BadRequestException("Unsupported image content type");
        }
        if (file.getSize() > MAX_IMAGE_BYTES) {
            throw new BadRequestException("About images must not exceed 10 MB");
        }
    }

    private String fileName(MultipartFile file) {
        String original = file == null ? null : file.getOriginalFilename();
        if (original == null || original.isBlank()) {
            throw new BadRequestException("Image file name is required");
        }
        String normalized = original.replace('\\', '/');
        String name = normalized.substring(normalized.lastIndexOf('/') + 1).trim();
        if (name.isBlank() || name.length() > 255) {
            throw new BadRequestException("Image file name is invalid");
        }
        return name;
    }

    private String contentType(MultipartFile file, String fileName) {
        String supplied = file.getContentType();
        if (supplied != null && !supplied.isBlank()
                && !"application/octet-stream".equalsIgnoreCase(supplied)) {
            return supplied.toLowerCase(Locale.ROOT);
        }
        int dot = fileName.lastIndexOf('.');
        String extension = dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
        String inferred = CONTENT_TYPES_BY_EXTENSION.get(extension);
        if (inferred == null) {
            throw new BadRequestException("Could not determine the image content type");
        }
        return inferred;
    }
}
