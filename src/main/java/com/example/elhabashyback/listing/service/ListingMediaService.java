package com.example.elhabashyback.listing.service;

import com.example.elhabashyback.common.exception.BadRequestException;
import com.example.elhabashyback.listing.dto.ListingMediaResponse;
import com.example.elhabashyback.listing.entity.MediaRole;
import com.example.elhabashyback.listing.entity.MediaType;
import com.example.elhabashyback.media.exception.MediaUploadException;
import com.example.elhabashyback.media.service.CloudinaryUploadClient;
import com.example.elhabashyback.media.service.CloudinaryUploadResult;
import com.example.elhabashyback.media.service.MediaStagingStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ListingMediaService {

    private static final long MAX_IMAGE_BYTES = 20L * 1024L * 1024L;
    private static final long MAX_VIDEO_BYTES = 5L * 1024L * 1024L * 1024L;
    private static final Set<String> IMAGE_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );
    private static final Set<String> VIDEO_CONTENT_TYPES = Set.of(
            "video/mp4", "video/webm", "video/quicktime", "video/x-matroska"
    );
    private static final Map<String, String> CONTENT_TYPES_BY_EXTENSION = Map.of(
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "png", "image/png",
            "webp", "image/webp",
            "gif", "image/gif",
            "mp4", "video/mp4",
            "webm", "video/webm",
            "mov", "video/quicktime",
            "mkv", "video/x-matroska"
    );

    private final CloudinaryUploadClient cloudinaryUploadClient;
    private final ListingMediaStateService stateService;
    private final MediaStagingStorage stagingStorage;
    private final ListingVideoUploadWorker videoUploadWorker;

    public ListingMediaResponse uploadImage(Long listingId, MultipartFile file, MediaRole role) {
        cloudinaryUploadClient.ensureConfigured();
        String fileName = fileName(file);
        String contentType = contentType(file, fileName);
        validateImage(file, contentType, role);
        PendingListingMedia pending = stateService.createPending(
                listingId, MediaType.IMAGE, role, fileName, contentType, file.getSize());
        try {
            CloudinaryUploadResult result = cloudinaryUploadClient.uploadImage(
                    file, pending.publicId(), contentType);
            return stateService.markReady(listingId, pending.mediaId(), result);
        } catch (RuntimeException exception) {
            stateService.markFailed(listingId, pending.mediaId(), exception.getMessage());
            throw exception;
        }
    }

    public ListingMediaResponse acceptVideo(Long listingId, MultipartFile file) {
        cloudinaryUploadClient.ensureConfigured();
        String fileName = fileName(file);
        String contentType = contentType(file, fileName);
        validateVideo(file, contentType);
        PendingListingMedia pending = stateService.createPending(
                listingId, MediaType.VIDEO, MediaRole.VIDEO, fileName, contentType, file.getSize());
        Path stagedFile;
        try {
            stagedFile = stagingStorage.stage(file, pending.mediaId());
        } catch (RuntimeException exception) {
            stateService.markFailed(listingId, pending.mediaId(), exception.getMessage());
            throw exception;
        }

        VideoUploadJob job = new VideoUploadJob(
                listingId,
                pending.mediaId(),
                pending.publicId(),
                fileName,
                contentType,
                file.getSize(),
                stagedFile
        );
        try {
            videoUploadWorker.upload(job);
        } catch (RuntimeException exception) {
            try {
                stagingStorage.delete(stagedFile);
            } finally {
                stateService.markFailed(listingId, pending.mediaId(), "Video upload queue is unavailable");
            }
            throw new MediaUploadException("Video upload queue is unavailable", exception);
        }
        return pending.response();
    }

    public ListingMediaResponse get(Long listingId, Long mediaId) {
        return stateService.get(listingId, mediaId);
    }

    public void delete(Long listingId, Long mediaId) {
        stateService.delete(listingId, mediaId);
    }

    private void validateImage(MultipartFile file, String contentType, MediaRole role) {
        validateNotEmpty(file);
        if (role == MediaRole.VIDEO) {
            throw new BadRequestException("Image role must be thumbnail or gallery");
        }
        if (!IMAGE_CONTENT_TYPES.contains(contentType)) {
            throw new BadRequestException("Unsupported image content type");
        }
        if (file.getSize() > MAX_IMAGE_BYTES) {
            throw new BadRequestException("Each image must not exceed 20 MB");
        }
    }

    private void validateVideo(MultipartFile file, String contentType) {
        validateNotEmpty(file);
        if (!VIDEO_CONTENT_TYPES.contains(contentType)) {
            throw new BadRequestException("Unsupported video content type");
        }
        if (file.getSize() > MAX_VIDEO_BYTES) {
            throw new BadRequestException("Video must not exceed 5 GB");
        }
    }

    private void validateNotEmpty(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Media file is required and cannot be empty");
        }
    }

    private String fileName(MultipartFile file) {
        String original = file == null ? null : file.getOriginalFilename();
        if (original == null || original.isBlank()) {
            throw new BadRequestException("Media file name is required");
        }
        String normalized = original.replace('\\', '/');
        String name = normalized.substring(normalized.lastIndexOf('/') + 1).trim();
        if (name.isBlank() || name.length() > 255) {
            throw new BadRequestException("Media file name is invalid");
        }
        return name;
    }

    private String contentType(MultipartFile file, String fileName) {
        String supplied = file.getContentType();
        if (supplied != null && !supplied.isBlank() && !"application/octet-stream".equalsIgnoreCase(supplied)) {
            return supplied.toLowerCase(Locale.ROOT);
        }
        int dot = fileName.lastIndexOf('.');
        String extension = dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
        String inferred = CONTENT_TYPES_BY_EXTENSION.get(extension);
        if (inferred == null) {
            throw new BadRequestException("Could not determine the media content type");
        }
        return inferred;
    }
}
