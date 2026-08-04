package com.example.elhabashyback.listing.service;

import com.example.elhabashyback.common.exception.BadRequestException;
import com.example.elhabashyback.common.exception.ConflictException;
import com.example.elhabashyback.common.exception.ResourceNotFoundException;
import com.example.elhabashyback.configuration.media.CloudinaryProperties;
import com.example.elhabashyback.listing.dto.CompleteMediaUploadRequest;
import com.example.elhabashyback.listing.dto.CreateMediaUploadRequest;
import com.example.elhabashyback.listing.dto.ListingMediaResponse;
import com.example.elhabashyback.listing.dto.MediaUploadTicketResponse;
import com.example.elhabashyback.listing.entity.Listing;
import com.example.elhabashyback.listing.entity.ListingMedia;
import com.example.elhabashyback.listing.entity.MediaRole;
import com.example.elhabashyback.listing.entity.MediaType;
import com.example.elhabashyback.listing.entity.MediaUploadStatus;
import com.example.elhabashyback.listing.repository.ListingMediaRepository;
import com.example.elhabashyback.listing.repository.ListingRepository;
import com.example.elhabashyback.media.exception.MediaUploadException;
import com.example.elhabashyback.media.service.CloudinarySignatureService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListingMediaService {

    public static final int VIDEO_CHUNK_SIZE = 6 * 1024 * 1024;
    private static final long MAX_IMAGE_BYTES = 20L * 1024L * 1024L;
    private static final long MAX_VIDEO_BYTES = 5L * 1024L * 1024L * 1024L;
    private static final Set<String> IMAGE_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );
    private static final Set<String> VIDEO_CONTENT_TYPES = Set.of(
            "video/mp4", "video/webm", "video/quicktime", "video/x-matroska"
    );

    private final ListingRepository listingRepository;
    private final ListingMediaRepository mediaRepository;
    private final CloudinaryProperties cloudinaryProperties;
    private final CloudinarySignatureService signatureService;

    @Transactional
    public MediaUploadTicketResponse createUpload(Long listingId, CreateMediaUploadRequest request) {
        requireCloudinaryConfiguration();
        Listing listing = getListing(listingId);
        String contentType = request.contentType().trim().toLowerCase(Locale.ROOT);
        MediaType mediaType = resolveMediaType(contentType);
        validateRoleAndSize(listingId, mediaType, request.role(), request.bytes());

        String publicId = buildPublicId(listingId);
        long timestamp = Instant.now().getEpochSecond();
        ListingMedia media = new ListingMedia();
        media.setListing(listing);
        media.setMediaType(mediaType);
        media.setMediaRole(request.role());
        media.setUploadStatus(MediaUploadStatus.UPLOADING);
        media.setFileName(request.fileName().trim());
        media.setContentType(contentType);
        media.setExpectedBytes(request.bytes());
        media.setPublicId(publicId);
        media.setDisplayOrder(mediaRepository.findMaximumDisplayOrder(listingId) + 1);
        mediaRepository.saveAndFlush(media);

        String resourceType = mediaType == MediaType.VIDEO ? "video" : "image";
        return new MediaUploadTicketResponse(
                ListingMediaResponse.from(media),
                "https://api.cloudinary.com/v1_1/" + cloudinaryProperties.cloudName() + "/" + resourceType + "/upload",
                cloudinaryProperties.cloudName(),
                cloudinaryProperties.apiKey(),
                timestamp,
                signatureService.signUpload(publicId, timestamp),
                publicId,
                resourceType,
                VIDEO_CHUNK_SIZE,
                Instant.ofEpochSecond(timestamp + 3600)
        );
    }

    @Transactional
    public ListingMediaResponse completeUpload(
            Long listingId,
            Long mediaId,
            CompleteMediaUploadRequest request
    ) {
        ListingMedia media = getMedia(listingId, mediaId);
        if (media.getUploadStatus() == MediaUploadStatus.READY) {
            return ListingMediaResponse.from(media);
        }
        if (media.getUploadStatus() == MediaUploadStatus.FAILED) {
            throw new ConflictException("Failed media upload cannot be completed");
        }
        validateCloudinaryResponse(media, request);

        media.setUploadStatus(MediaUploadStatus.READY);
        media.setMediaUrl(request.secureUrl().trim());
        media.setFormat(request.format().trim().toLowerCase(Locale.ROOT));
        media.setWidth(request.width());
        media.setHeight(request.height());
        media.setActualBytes(request.bytes());
        media.setDurationSeconds(request.duration());
        media.setCloudinaryVersion(request.version());
        media.setFailureReason(null);
        mediaRepository.flush();
        return ListingMediaResponse.from(media);
    }

    @Transactional
    public ListingMediaResponse failUpload(Long listingId, Long mediaId, String reason) {
        ListingMedia media = getMedia(listingId, mediaId);
        if (media.getUploadStatus() == MediaUploadStatus.READY) {
            throw new ConflictException("Ready media cannot be marked as failed");
        }
        media.setUploadStatus(MediaUploadStatus.FAILED);
        media.setFailureReason(reason.trim());
        mediaRepository.flush();
        return ListingMediaResponse.from(media);
    }

    @Transactional
    public void delete(Long listingId, Long mediaId) {
        ListingMedia media = getMedia(listingId, mediaId);
        mediaRepository.delete(media);
        mediaRepository.flush();
    }

    private void validateCloudinaryResponse(ListingMedia media, CompleteMediaUploadRequest request) {
        String expectedResourceType = media.getMediaType() == MediaType.VIDEO ? "video" : "image";
        if (!media.getPublicId().equals(request.publicId())) {
            throw new BadRequestException("Cloudinary publicId does not match the upload ticket");
        }
        if (!expectedResourceType.equalsIgnoreCase(request.resourceType())) {
            throw new BadRequestException("Cloudinary resource type does not match the uploaded media");
        }
        if (request.bytes() != media.getExpectedBytes()) {
            throw new BadRequestException("Uploaded media size does not match the upload ticket");
        }
        if (media.getMediaType() == MediaType.VIDEO && request.duration() == null) {
            throw new BadRequestException("Cloudinary video duration is required");
        }
        if (media.getMediaType() == MediaType.IMAGE && request.duration() != null) {
            throw new BadRequestException("Image media cannot have a video duration");
        }
        String expectedUrlPrefix = "https://res.cloudinary.com/" + cloudinaryProperties.cloudName() + "/";
        if (!request.secureUrl().startsWith(expectedUrlPrefix)) {
            throw new BadRequestException("Cloudinary secure URL is invalid");
        }
        if (!signatureService.verifyUploadResponse(request.publicId(), request.version(), request.signature())) {
            throw new BadRequestException("Cloudinary response signature is invalid");
        }
    }

    private MediaType resolveMediaType(String contentType) {
        if (IMAGE_CONTENT_TYPES.contains(contentType)) {
            return MediaType.IMAGE;
        }
        if (VIDEO_CONTENT_TYPES.contains(contentType)) {
            return MediaType.VIDEO;
        }
        throw new BadRequestException("Unsupported image or video content type");
    }

    private void validateRoleAndSize(Long listingId, MediaType type, MediaRole role, long bytes) {
        if (type == MediaType.IMAGE) {
            if (role == MediaRole.VIDEO) {
                throw new BadRequestException("Image media role must be thumbnail or gallery");
            }
            if (bytes > MAX_IMAGE_BYTES) {
                throw new BadRequestException("Each image must not exceed 20 MB");
            }
        } else {
            if (role != MediaRole.VIDEO) {
                throw new BadRequestException("Video media role must be video");
            }
            if (bytes > MAX_VIDEO_BYTES) {
                throw new BadRequestException("Video must not exceed 5 GB");
            }
        }
        if ((role == MediaRole.THUMBNAIL || role == MediaRole.VIDEO)
                && mediaRepository.existsByListingIdAndMediaRoleAndUploadStatusNot(
                listingId,
                role,
                MediaUploadStatus.FAILED
        )) {
            throw new ConflictException("Listing already has a " + role.value());
        }
    }

    private String buildPublicId(Long listingId) {
        String baseFolder = cloudinaryProperties.folder() == null || cloudinaryProperties.folder().isBlank()
                ? "el-habashy/listings"
                : cloudinaryProperties.folder().replaceAll("^/+|/+$", "");
        return baseFolder + "/" + listingId + "/" + UUID.randomUUID();
    }

    private void requireCloudinaryConfiguration() {
        if (!cloudinaryProperties.isConfigured()) {
            throw new MediaUploadException("Cloudinary is not configured");
        }
    }

    private Listing getListing(Long listingId) {
        return listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));
    }

    private ListingMedia getMedia(Long listingId, Long mediaId) {
        return mediaRepository.findByIdAndListingId(mediaId, listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing media not found"));
    }
}
