package com.example.elhabashyback.listing.dto;

import com.example.elhabashyback.common.dto.LocalizedTextRequest;
import com.example.elhabashyback.listing.entity.ListingStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record UpsertListingRequest(
        @Pattern(regexp = "^$|^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "slug must contain lowercase letters, numbers, and hyphens only")
        @Size(max = 180) String slug,
        @NotNull @Valid LocalizedTextRequest title,
        @NotNull @Valid LocalizedTextRequest summary,
        @NotNull @Valid LocalizedTextRequest description,
        @NotBlank @Size(max = 50) String category,
        @NotNull ListingStatus status,
        @NotNull @Valid LocalizedTextRequest city,
        @NotNull @Valid LocalizedTextRequest location,
        @NotNull @Valid LocalizedTextRequest priceLabel,
        @NotBlank @Size(max = 255) String measureLabel,
        @NotNull Boolean featured,
        @NotNull @Size(max = 20) List<@Valid ListingSpecificationRequest> specs,
        LocalDate publishDate,
        LocalDate expireDate,
        LocalDate auctionDate,
        LocalTime auctionTime,
        @Valid LocalizedTextRequest beneficiary,
        @Valid LocalizedTextRequest venue,
        @Valid LocalizedTextRequest announcementSource,
        @Valid LocalizedTextRequest notes,
        @Size(max = 4096) String mapUrl,
        @Pattern(regexp = "^$|^\\+?[0-9][0-9\\s-]{7,38}$", message = "whatsappPhone must be a valid phone number")
        String whatsappPhone,
        @Valid LocalizedTextRequest seoTitle,
        @Valid LocalizedTextRequest seoDescription,
        @Valid LocalizedTextRequest seoKeywords
) {
}
