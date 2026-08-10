package com.example.elhabashyback.listing.dto;

import com.example.elhabashyback.listing.entity.Listing;

public record ListingEngagementResponse(
        Long listingId,
        String slug,
        long views,
        long whatsappClicks
) {
    public static ListingEngagementResponse from(Listing listing) {
        return new ListingEngagementResponse(
                listing.getId(),
                listing.getSlug(),
                listing.getViews(),
                listing.getWhatsappClicks()
        );
    }
}
