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
                media.getDurationSeconds(),
                media.getDisplayOrder(),
                media.getFailureReason()
        );
    }
}
