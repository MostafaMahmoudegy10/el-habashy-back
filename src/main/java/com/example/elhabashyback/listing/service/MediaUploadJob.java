package com.example.elhabashyback.listing.service;

import com.example.elhabashyback.listing.entity.MediaType;

import java.nio.file.Path;

public record MediaUploadJob(
        Long listingId,
        Long mediaId,
        MediaType mediaType,
        String publicId,
        String fileName,
        String contentType,
        long bytes,
        Path stagedFile
) {
}
