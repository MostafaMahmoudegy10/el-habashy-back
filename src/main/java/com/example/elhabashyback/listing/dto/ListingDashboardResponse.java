package com.example.elhabashyback.listing.dto;

import java.util.List;

public record ListingDashboardResponse(
        long totalListings,
        long activeListings,
        long totalViews,
        long totalWhatsappClicks,
        ListingResponse mostViewedListing,
        ListingResponse mostContactedListing,
        List<ListingResponse> topContactedListings
) {
}
