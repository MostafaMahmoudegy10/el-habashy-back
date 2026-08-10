package com.example.elhabashyback.listing.service;

import com.example.elhabashyback.listing.entity.MediaType;
import com.example.elhabashyback.media.service.CloudinaryUploadClient;
import com.example.elhabashyback.media.service.CloudinaryUploadResult;
import com.example.elhabashyback.media.service.MediaStagingStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class ListingMediaUploadWorker {

    private final CloudinaryUploadClient cloudinaryUploadClient;
    private final ListingMediaStateService stateService;
    private final MediaStagingStorage stagingStorage;

    @Async("mediaUploadExecutor")
    public void upload(MediaUploadJob job) {
        process(job);
    }

    void process(MediaUploadJob job) {
        try {
            stateService.markProcessing(job.listingId(), job.mediaId());
            CloudinaryUploadResult result = job.mediaType() == MediaType.IMAGE
                    ? cloudinaryUploadClient.uploadImage(
                    job.stagedFile(),
                    job.fileName(),
                    job.publicId(),
                    job.contentType(),
                    job.bytes())
                    : uploadVideo(job);
            stateService.markReady(job.listingId(), job.mediaId(), result);
        } catch (Exception exception) {
            try {
                stateService.markFailed(job.listingId(), job.mediaId(), exception.getMessage());
            } catch (Exception ignored) {
                // The original upload failure remains the primary failure.
            }
        } finally {
            try {
                stagingStorage.delete(job.stagedFile());
            } catch (Exception ignored) {
                // Staging cleanup must not change the persisted upload result.
            }
        }
    }

    private CloudinaryUploadResult uploadVideo(MediaUploadJob job) {
        AtomicInteger lastPersistedPercent = new AtomicInteger(-1);
        return cloudinaryUploadClient.uploadVideo(
                job.stagedFile(),
                job.fileName(),
                job.contentType(),
                job.publicId(),
                job.bytes(),
                uploadedBytes -> persistProgress(job, uploadedBytes, lastPersistedPercent)
        );
    }

    private void persistProgress(
            MediaUploadJob job,
            long uploadedBytes,
            AtomicInteger lastPersistedPercent
    ) {
        int percent = (int) Math.min(100, uploadedBytes * 100 / job.bytes());
        int previous = lastPersistedPercent.getAndSet(percent);
        if (percent != previous) {
            stateService.updateProgress(job.listingId(), job.mediaId(), uploadedBytes);
        }
    }
}
