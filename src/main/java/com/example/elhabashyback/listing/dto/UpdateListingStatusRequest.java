package com.example.elhabashyback.listing.dto;

import com.example.elhabashyback.listing.entity.ListingStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateListingStatusRequest(
        @NotNull ListingStatus status
) {
}
