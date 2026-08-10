package com.example.elhabashyback.listing.service;

import com.example.elhabashyback.listing.entity.MediaType;
import com.example.elhabashyback.media.service.CloudinaryUploadClient;
import com.example.elhabashyback.media.service.CloudinaryUploadResult;
import com.example.elhabashyback.media.service.MediaStagingStorage;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ListingMediaUploadWorkerTests {

    private final CloudinaryUploadClient cloudinary = mock(CloudinaryUploadClient.class);
    private final ListingMediaStateService stateService = mock(ListingMediaStateService.class);
    private final MediaStagingStorage stagingStorage = mock(MediaStagingStorage.class);
    private final ListingMediaUploadWorker worker = new ListingMediaUploadWorker(
            cloudinary, stateService, stagingStorage);

    @Test
    void imageWorkerStoresVerifiedResultAndCleansStagedFile() {
        Path stagedFile = Path.of("staged", "thumbnail.png");
        MediaUploadJob job = new MediaUploadJob(
                12L,
                34L,
                MediaType.IMAGE,
                "listings/12/thumbnail",
                "thumbnail.png",
                "image/png",
                4,
                stagedFile
        );
        CloudinaryUploadResult result = result(job, "image", "png", null);
        when(cloudinary.uploadImage(
                stagedFile,
                job.fileName(),
                job.publicId(),
                job.contentType(),
                job.bytes()
        )).thenReturn(result);

        worker.process(job);

        var ordered = inOrder(stateService);
        ordered.verify(stateService).markProcessing(job.listingId(), job.mediaId());
        ordered.verify(stateService).markReady(job.listingId(), job.mediaId(), result);
        verify(stagingStorage).delete(stagedFile);
    }

    @Test
    void videoWorkerPersistsProgressAndFinalResult() {
        Path stagedFile = Path.of("staged", "auction.mp4");
        MediaUploadJob job = new MediaUploadJob(
                12L,
                35L,
                MediaType.VIDEO,
                "listings/12/video",
                "auction.mp4",
                "video/mp4",
                5,
                stagedFile
        );
        CloudinaryUploadResult result = result(job, "video", "mp4", 12.5);
        when(cloudinary.uploadVideo(
                eq(stagedFile),
                eq(job.fileName()),
                eq(job.contentType()),
                eq(job.publicId()),
                eq(job.bytes()),
                any()
        )).thenAnswer(invocation -> {
            java.util.function.LongConsumer progress = invocation.getArgument(5);
            progress.accept(5);
            return result;
        });

        worker.process(job);

        verify(stateService).updateProgress(job.listingId(), job.mediaId(), 5);
        verify(stateService).markReady(job.listingId(), job.mediaId(), result);
        verify(stagingStorage).delete(stagedFile);
    }

    @Test
    void workerMarksFailedUploadAndStillCleansStagedFile() {
        Path stagedFile = Path.of("staged", "broken.png");
        MediaUploadJob job = new MediaUploadJob(
                12L,
                36L,
                MediaType.IMAGE,
                "listings/12/broken",
                "broken.png",
                "image/png",
                4,
                stagedFile
        );
        when(cloudinary.uploadImage(
                stagedFile,
                job.fileName(),
                job.publicId(),
                job.contentType(),
                job.bytes()
        )).thenThrow(new IllegalStateException("Cloudinary unavailable"));

        worker.process(job);

        verify(stateService).markFailed(job.listingId(), job.mediaId(), "Cloudinary unavailable");
        verify(stagingStorage).delete(stagedFile);
    }

    private CloudinaryUploadResult result(
            MediaUploadJob job,
            String resourceType,
            String format,
            Double duration
    ) {
        return new CloudinaryUploadResult(
                "https://res.cloudinary.com/test-cloud/" + resourceType + "/upload/" + job.fileName(),
                job.publicId(),
                resourceType,
                format,
                800,
                600,
                job.bytes(),
                duration,
                1719307544L,
                "verified"
        );
    }
}
