package com.example.elhabashyback.listing.controller;

import com.example.elhabashyback.common.dto.PageResponse;
import com.example.elhabashyback.listing.dto.ListingResponse;
import com.example.elhabashyback.listing.service.ListingService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/listings")
@RequiredArgsConstructor
@Validated
public class PublicListingController {

    private final ListingService listingService;

    @GetMapping
    PageResponse<ListingResponse> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean featured,
            @RequestParam(name = "q", required = false) String search,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "12") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        return listingService.listPublic(category, status, featured, search, page, size, sort);
    }

    @GetMapping("/{slug}")
    ListingResponse getBySlug(@PathVariable String slug) {
        return listingService.getPublicBySlug(slug);
    }
}
