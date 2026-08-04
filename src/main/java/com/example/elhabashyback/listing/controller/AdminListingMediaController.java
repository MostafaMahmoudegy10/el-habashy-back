package com.example.elhabashyback.listing.controller;

import com.example.elhabashyback.listing.dto.CompleteMediaUploadRequest;
import com.example.elhabashyback.listing.dto.CreateMediaUploadRequest;
import com.example.elhabashyback.listing.dto.FailMediaUploadRequest;
import com.example.elhabashyback.listing.dto.ListingMediaResponse;
import com.example.elhabashyback.listing.dto.MediaUploadTicketResponse;
import com.example.elhabashyback.listing.service.ListingMediaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/listings/{listingId}/media")
@RequiredArgsConstructor
public class AdminListingMediaController {

    private final ListingMediaService mediaService;

    @PostMapping("/uploads")
    MediaUploadTicketResponse createUpload(
            @PathVariable Long listingId,
            @Valid @RequestBody CreateMediaUploadRequest request
    ) {
        return mediaService.createUpload(listingId, request);
    }

    @PostMapping("/{mediaId}/complete")
    ListingMediaResponse completeUpload(
            @PathVariable Long listingId,
            @PathVariable Long mediaId,
            @Valid @RequestBody CompleteMediaUploadRequest request
    ) {
        return mediaService.completeUpload(listingId, mediaId, request);
    }

    @PostMapping("/{mediaId}/fail")
    ListingMediaResponse failUpload(
            @PathVariable Long listingId,
            @PathVariable Long mediaId,
            @Valid @RequestBody FailMediaUploadRequest request
    ) {
        return mediaService.failUpload(listingId, mediaId, request.reason());
    }

    @DeleteMapping("/{mediaId}")
    ResponseEntity<Void> delete(
            @PathVariable Long listingId,
            @PathVariable Long mediaId
    ) {
        mediaService.delete(listingId, mediaId);
        return ResponseEntity.noContent().build();
    }
}
