package com.example.elhabashyback.listing.dto;

import java.util.List;

public record PublicListingInsightsResponse(
        long totalListings,
        long activeListings,
        long totalViews,
        long totalWhatsappClicks,
        ListingResponse mostViewedListing,
        List<ListingResponse> topContactedListings
) {
}
