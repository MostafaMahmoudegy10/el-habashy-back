package com.example.elhabashyback.listing.service;

import com.example.elhabashyback.common.exception.ConflictException;
import com.example.elhabashyback.common.exception.ResourceNotFoundException;
import com.example.elhabashyback.configuration.media.CloudinaryProperties;
import com.example.elhabashyback.listing.dto.ListingMediaResponse;
import com.example.elhabashyback.listing.entity.Listing;
import com.example.elhabashyback.listing.entity.ListingMedia;
import com.example.elhabashyback.listing.entity.MediaRole;
import com.example.elhabashyback.listing.entity.MediaType;
import com.example.elhabashyback.listing.entity.MediaUploadStatus;
import com.example.elhabashyback.listing.repository.ListingMediaRepository;
import com.example.elhabashyback.listing.repository.ListingRepository;
import com.example.elhabashyback.media.service.CloudinaryUploadResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ListingMediaStateService {

    private final ListingRepository listingRepository;
    private final ListingMediaRepository mediaRepository;
    private final CloudinaryProperties cloudinaryProperties;

    @Transactional
    public PendingListingMedia createPending(
            Long listingId,
            MediaType mediaType,
            MediaRole role,
            String fileName,
            String contentType,
            long bytes
    ) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));
        if ((role == MediaRole.THUMBNAIL || role == MediaRole.VIDEO)
                && mediaRepository.existsByListingIdAndMediaRoleAndUploadStatusNot(
                listingId, role, MediaUploadStatus.FAILED)) {
            throw new ConflictException("Listing already has a " + role.value());
        }

        ListingMedia media = new ListingMedia();
        media.setListing(listing);
        media.setMediaType(mediaType);
        media.setMediaRole(role);
        media.setUploadStatus(MediaUploadStatus.UPLOADING);
        media.setFileName(fileName);
        media.setContentType(contentType);
        media.setExpectedBytes(bytes);
        media.setUploadedBytes(0);
        media.setDisplayOrder(mediaRepository.findMaximumDisplayOrder(listingId) + 1);
        listing.addMedia(media);
        mediaRepository.saveAndFlush(media);
        String publicId = buildPublicId(listingId);
        return new PendingListingMedia(
                listingId,
                media.getId(),
                mediaType,
                publicId,
                fileName,
                contentType,
                bytes,
                ListingMediaResponse.from(media)
        );
    }

    @Transactional
    public void markProcessing(Long listingId, Long mediaId) {
        ListingMedia media = getEntity(listingId, mediaId);
        if (media.getUploadStatus() == MediaUploadStatus.UPLOADING) {
            media.setUploadStatus(MediaUploadStatus.PROCESSING);
            mediaRepository.flush();
        }
    }

    @Transactional
    public void updateProgress(Long listingId, Long mediaId, long uploadedBytes) {
        ListingMedia media = getEntity(listingId, mediaId);
        if (media.getUploadStatus() == MediaUploadStatus.PROCESSING) {
            media.setUploadedBytes(Math.min(uploadedBytes, media.getExpectedBytes()));
            mediaRepository.flush();
        }
    }

    @Transactional
    public ListingMediaResponse markReady(
            Long listingId,
            Long mediaId,
            CloudinaryUploadResult result
    ) {
        ListingMedia media = getEntity(listingId, mediaId);
        media.setUploadStatus(MediaUploadStatus.READY);
        media.setPublicId(result.publicId());
        media.setMediaUrl(result.secureUrl());
        media.setFormat(result.format());
        media.setWidth(result.width());
        media.setHeight(result.height());
        media.setActualBytes(result.bytes());
        media.setUploadedBytes(result.bytes());
        media.setDurationSeconds(result.duration());
        media.setCloudinaryVersion(result.version());
        media.setFailureReason(null);
        mediaRepository.flush();
        return ListingMediaResponse.from(media);
    }

    @Transactional
    public void markFailed(Long listingId, Long mediaId, String reason) {
        ListingMedia media = getEntity(listingId, mediaId);
        media.setUploadStatus(MediaUploadStatus.FAILED);
        media.setFailureReason(trimReason(reason));
        mediaRepository.flush();
    }

    @Transactional(readOnly = true)
    public ListingMediaResponse get(Long listingId, Long mediaId) {
        return ListingMediaResponse.from(getEntity(listingId, mediaId));
    }

    @Transactional
    public void delete(Long listingId, Long mediaId) {
        ListingMedia media = getEntity(listingId, mediaId);
        mediaRepository.delete(media);
        mediaRepository.flush();
    }

    private ListingMedia getEntity(Long listingId, Long mediaId) {
        return mediaRepository.findByIdAndListingId(mediaId, listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing media not found"));
    }

    private String buildPublicId(Long listingId) {
        String baseFolder = cloudinaryProperties.folder() == null || cloudinaryProperties.folder().isBlank()
                ? "el-habashy/listings"
                : cloudinaryProperties.folder().replaceAll("^/+|/+$", "");
        return baseFolder + "/" + listingId + "/" + UUID.randomUUID();
    }

    private String trimReason(String reason) {
        String value = reason == null || reason.isBlank() ? "Media upload failed" : reason.trim();
        return value.length() <= 500 ? value : value.substring(0, 500);
    }
}
