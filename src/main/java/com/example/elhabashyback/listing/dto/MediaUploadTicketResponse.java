package com.example.elhabashyback.listing.dto;

import java.time.Instant;

public record MediaUploadTicketResponse(
        ListingMediaResponse media,
        String uploadUrl,
        String cloudName,
        String apiKey,
        long timestamp,
        String signature,
        String publicId,
        String resourceType,
        int chunkSize,
        Instant expiresAt
) {
}
