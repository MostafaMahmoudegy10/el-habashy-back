package com.example.elhabashyback.listing.controller;

import com.example.elhabashyback.common.dto.PageResponse;
import com.example.elhabashyback.listing.dto.ListingResponse;
import com.example.elhabashyback.listing.dto.UpdateListingStatusRequest;
import com.example.elhabashyback.listing.dto.UpsertListingRequest;
import com.example.elhabashyback.listing.service.ListingService;
import com.example.elhabashyback.listing.service.ListingSubmissionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/listings")
@RequiredArgsConstructor
@Validated
public class AdminListingController {

    private final ListingService listingService;
    private final ListingSubmissionService submissionService;

    @GetMapping
    PageResponse<ListingResponse> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean featured,
            @RequestParam(name = "q", required = false) String search,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        return listingService.listAdmin(category, status, featured, search, page, size, sort);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<ListingResponse> create(
            @Valid @RequestPart("listing") UpsertListingRequest request,
            @RequestPart("thumbnail") MultipartFile thumbnail,
            @RequestPart(name = "gallery", required = false) List<MultipartFile> gallery,
            @RequestPart(name = "video", required = false) MultipartFile video
    ) {
        ListingResponse response = submissionService.submit(
                request,
                thumbnail,
                gallery == null ? List.of() : gallery,
                video
        );
        return ResponseEntity.accepted()
                .location(URI.create("/api/v1/public/listings/" + response.slug()))
                .body(response);
    }

    @PutMapping("/{id}")
    ListingResponse update(@PathVariable Long id, @Valid @RequestBody UpsertListingRequest request) {
        return listingService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    ListingResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateListingStatusRequest request
    ) {
        return listingService.updateStatus(id, request.status());
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable Long id) {
        listingService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
