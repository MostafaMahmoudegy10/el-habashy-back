package com.example.elhabashyback.listing.repository;

import com.example.elhabashyback.listing.entity.ListingMedia;
import com.example.elhabashyback.listing.entity.MediaRole;
import com.example.elhabashyback.listing.entity.MediaUploadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ListingMediaRepository extends JpaRepository<ListingMedia, Long> {

    Optional<ListingMedia> findByIdAndListingId(Long id, Long listingId);

    List<ListingMedia> findAllByListingIdOrderByDisplayOrderAsc(Long listingId);

    boolean existsByListingIdAndMediaRoleAndUploadStatusNot(
            Long listingId,
            MediaRole role,
            MediaUploadStatus excludedStatus
    );

    @Query("select coalesce(max(media.displayOrder), -1) from ListingMedia media where media.listing.id = :listingId")
    int findMaximumDisplayOrder(@Param("listingId") Long listingId);
}
