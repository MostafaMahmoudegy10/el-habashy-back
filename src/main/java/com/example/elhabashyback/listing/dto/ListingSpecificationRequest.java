package com.example.elhabashyback.listing.dto;

import com.example.elhabashyback.common.dto.LocalizedTextRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record ListingSpecificationRequest(
        @NotNull @Valid LocalizedTextRequest label,
        @NotNull @Valid LocalizedTextRequest value
) {
}
