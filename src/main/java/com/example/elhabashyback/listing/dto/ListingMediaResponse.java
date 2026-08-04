package com.example.elhabashyback.listing.dto;

import com.example.elhabashyback.listing.entity.ListingMedia;
import com.example.elhabashyback.listing.entity.MediaRole;
import com.example.elhabashyback.listing.entity.MediaType;
import com.example.elhabashyback.listing.entity.MediaUploadStatus;

public record ListingMediaResponse(
        Long id,
        MediaType type,
        MediaRole role,
        MediaUploadStatus status,
        String url,
        String fileName,
        String contentType,
        String format,
        Integer width,
        Integer height,
        Long bytes,
        long expectedBytes,
        long uploadedBytes,
        int progress,
        Double durationSeconds,
        int displayOrder,
        String failureReason
) {
    public static ListingMediaResponse from(ListingMedia media) {
        return new ListingMediaResponse(
                media.getId(),
                media.getMediaType(),
                media.getMediaRole(),
                media.getUploadStatus(),
                media.getMediaUrl(),
                media.getFileName(),
                media.getContentType(),
                media.getFormat(),
                media.getWidth(),
                media.getHeight(),
                media.getActualBytes(),
                media.getExpectedBytes(),
                media.getUploadedBytes(),
                progress(media),
                media.getDurationSeconds(),
                media.getDisplayOrder(),
                media.getFailureReason()
        );
    }

    private static int progress(ListingMedia media) {
        if (media.getUploadStatus() == MediaUploadStatus.READY) {
            return 100;
        }
        if (media.getExpectedBytes() <= 0) {
            return 0;
        }
        return (int) Math.min(100, media.getUploadedBytes() * 100 / media.getExpectedBytes());
    }
}
