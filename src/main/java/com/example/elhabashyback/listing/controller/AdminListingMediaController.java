package com.example.elhabashyback.listing.controller;

import com.example.elhabashyback.common.exception.BadRequestException;
import com.example.elhabashyback.listing.dto.ListingMediaResponse;
import com.example.elhabashyback.listing.entity.MediaRole;
import com.example.elhabashyback.listing.service.ListingMediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/admin/listings/{listingId}/media")
@RequiredArgsConstructor
public class AdminListingMediaController {

    private final ListingMediaService mediaService;

    @PostMapping(value = "/images/{role}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<ListingMediaResponse> uploadImage(
            @PathVariable Long listingId,
            @PathVariable String role,
            @RequestPart("file") MultipartFile file
    ) {
        MediaRole mediaRole = parseImageRole(role);
        ListingMediaResponse response = mediaService.acceptImage(listingId, file, mediaRole);
        return ResponseEntity.accepted()
                .location(mediaLocation(listingId, response.id()))
                .body(response);
    }

    @PostMapping(value = "/videos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<ListingMediaResponse> uploadVideo(
            @PathVariable Long listingId,
            @RequestPart("file") MultipartFile file
    ) {
        ListingMediaResponse response = mediaService.acceptVideo(listingId, file);
        return ResponseEntity.accepted()
                .location(mediaLocation(listingId, response.id()))
                .body(response);
    }

    @GetMapping("/{mediaId}")
    ListingMediaResponse get(
            @PathVariable Long listingId,
            @PathVariable Long mediaId
    ) {
        return mediaService.get(listingId, mediaId);
    }

    @DeleteMapping("/{mediaId}")
    ResponseEntity<Void> delete(
            @PathVariable Long listingId,
            @PathVariable Long mediaId
    ) {
        mediaService.delete(listingId, mediaId);
        return ResponseEntity.noContent().build();
    }

    private MediaRole parseImageRole(String role) {
        try {
            MediaRole value = MediaRole.fromValue(role);
            if (value == MediaRole.VIDEO) {
                throw new BadRequestException("Image role must be thumbnail or gallery");
            }
            return value;
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException("Image role must be thumbnail or gallery");
        }
    }

    private URI mediaLocation(Long listingId, Long mediaId) {
        return URI.create("/api/v1/admin/listings/" + listingId + "/media/" + mediaId);
    }
}
