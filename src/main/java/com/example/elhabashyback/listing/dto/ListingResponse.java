package com.example.elhabashyback.listing.dto;

import com.example.elhabashyback.common.dto.LocalizedTextResponse;
import com.example.elhabashyback.listing.entity.Listing;
import com.example.elhabashyback.listing.entity.ListingStatus;
import com.example.elhabashyback.listing.entity.MediaType;
import com.example.elhabashyback.listing.entity.MediaUploadStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record ListingResponse(
        Long id,
        String slug,
        LocalizedTextResponse title,
        LocalizedTextResponse summary,
        LocalizedTextResponse description,
        String category,
        ListingStatus status,
        LocalizedTextResponse city,
        LocalizedTextResponse location,
        LocalizedTextResponse priceLabel,
        String measureLabel,
        boolean featured,
        List<String> images,
        List<ListingMediaResponse> media,
        List<ListingSpecificationResponse> specs,
        LocalDate publishDate,
        LocalDate expireDate,
        LocalDate auctionDate,
        LocalTime auctionTime,
        LocalizedTextResponse beneficiary,
        LocalizedTextResponse venue,
        LocalizedTextResponse announcementSource,
        LocalizedTextResponse notes,
        String mapUrl,
        String whatsappPhone,
        long views,
        long whatsappClicks,
        LocalizedTextResponse seoTitle,
        LocalizedTextResponse seoDescription,
        LocalizedTextResponse seoKeywords,
        Instant createdAt,
        Instant updatedAt
) {
    public static ListingResponse fromPublic(Listing listing) {
        return from(listing, false);
    }

    public static ListingResponse fromAdmin(Listing listing) {
        return from(listing, true);
    }

    private static ListingResponse from(Listing listing, boolean includePendingMedia) {
        List<ListingMediaResponse> media = listing.getMedia().stream()
                .filter(item -> includePendingMedia || item.getUploadStatus() == MediaUploadStatus.READY)
                .map(ListingMediaResponse::from)
                .toList();
        List<String> images = listing.getMedia().stream()
                .filter(item -> item.getUploadStatus() == MediaUploadStatus.READY)
                .filter(item -> item.getMediaType() == MediaType.IMAGE)
                .map(item -> item.getMediaUrl())
                .toList();
        return new ListingResponse(
                listing.getId(),
                listing.getSlug(),
                text(listing.getTitleAr(), listing.getTitleEn()),
                text(listing.getSummaryAr(), listing.getSummaryEn()),
                text(listing.getDescriptionAr(), listing.getDescriptionEn()),
                listing.getSector().getCode(),
                listing.getStatus(),
                text(listing.getCityAr(), listing.getCityEn()),
                text(listing.getLocationAr(), listing.getLocationEn()),
                text(listing.getPriceLabelAr(), listing.getPriceLabelEn()),
                listing.getMeasureLabel(),
                listing.isFeatured(),
                images,
                media,
                listing.getSpecifications().stream().map(ListingSpecificationResponse::from).toList(),
                listing.getPublishDate(),
                listing.getExpireDate(),
                listing.getAuctionDate(),
                listing.getAuctionTime(),
                optionalText(listing.getBeneficiaryAr(), listing.getBeneficiaryEn()),
                optionalText(listing.getVenueAr(), listing.getVenueEn()),
                optionalText(listing.getAnnouncementSourceAr(), listing.getAnnouncementSourceEn()),
                optionalText(listing.getNotesAr(), listing.getNotesEn()),
                listing.getMapUrl(),
                listing.getWhatsappPhone(),
                listing.getViews(),
                listing.getWhatsappClicks(),
                optionalText(listing.getSeoTitleAr(), listing.getSeoTitleEn()),
                optionalText(listing.getSeoDescriptionAr(), listing.getSeoDescriptionEn()),
                optionalText(listing.getSeoKeywordsAr(), listing.getSeoKeywordsEn()),
                listing.getCreatedAt(),
                listing.getUpdatedAt()
        );
    }

    private static LocalizedTextResponse text(String ar, String en) {
        return new LocalizedTextResponse(ar, en);
    }

    private static LocalizedTextResponse optionalText(String ar, String en) {
        return ar == null && en == null ? null : text(ar, en);
    }
}
