package com.example.elhabashyback.listing.service;

import java.nio.file.Path;

public record VideoUploadJob(
        Long listingId,
        Long mediaId,
        String publicId,
        String fileName,
        String contentType,
        long bytes,
        Path stagedFile
) {
}
