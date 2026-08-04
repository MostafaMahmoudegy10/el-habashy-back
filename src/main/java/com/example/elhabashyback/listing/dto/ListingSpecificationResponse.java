package com.example.elhabashyback.listing.dto;

import com.example.elhabashyback.common.dto.LocalizedTextResponse;
import com.example.elhabashyback.listing.entity.ListingSpecification;

public record ListingSpecificationResponse(
        LocalizedTextResponse label,
        LocalizedTextResponse value
) {
    public static ListingSpecificationResponse from(ListingSpecification specification) {
        return new ListingSpecificationResponse(
                new LocalizedTextResponse(specification.getLabelAr(), specification.getLabelEn()),
                new LocalizedTextResponse(specification.getValueAr(), specification.getValueEn())
        );
    }
}
