package com.example.elhabashyback.listing.service;

import com.example.elhabashyback.listing.dto.ListingResponse;
import com.example.elhabashyback.configuration.cache.CacheConfiguration;
import com.example.elhabashyback.listing.dto.UpsertListingRequest;
import com.example.elhabashyback.listing.entity.Listing;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListingSubmissionService {

    private final ListingService listingService;
    private final ListingMediaService mediaService;

    @Transactional
    @CacheEvict(cacheNames = CacheConfiguration.PUBLIC_LISTINGS, allEntries = true)
    public ListingResponse submit(
            UpsertListingRequest request,
            MultipartFile thumbnail,
            List<MultipartFile> gallery,
            MultipartFile video
    ) {
        Listing listing = listingService.createEntity(request);
        List<MediaUploadJob> jobs = mediaService.prepareSubmission(
                listing.getId(), thumbnail, gallery, video);
        dispatchAfterCommit(jobs);
        return ListingResponse.fromAdmin(listing);
    }

    private void dispatchAfterCommit(List<MediaUploadJob> jobs) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            mediaService.cleanup(jobs);
            throw new IllegalStateException("Listing submission requires an active transaction");
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                mediaService.dispatch(jobs);
            }

            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    mediaService.cleanup(jobs);
                }
            }
        });
    }
}
