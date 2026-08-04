package com.example.elhabashyback.listing.service;

import com.example.elhabashyback.listing.dto.ListingMediaResponse;
import com.example.elhabashyback.listing.entity.MediaType;

public record PendingListingMedia(
        Long listingId,
        Long mediaId,
        MediaType mediaType,
        String publicId,
        String fileName,
        String contentType,
        long bytes,
        ListingMediaResponse response
) {
}
