package com.example.elhabashyback.listing.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "listing_media")
@Getter
@Setter
public class ListingMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false, length = 20)
    private MediaType mediaType;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_role", nullable = false, length = 20)
    private MediaRole mediaRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "upload_status", nullable = false, length = 20)
    private MediaUploadStatus uploadStatus;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "content_type", length = 120)
    private String contentType;

    @Column(name = "expected_bytes", nullable = false)
    private long expectedBytes;

    @Column(name = "public_id", unique = true, length = 300)
    private String publicId;

    @Column(name = "media_url", columnDefinition = "text")
    private String mediaUrl;

    @Column(length = 50)
    private String format;

    private Integer width;

    private Integer height;

    @Column(name = "actual_bytes")
    private Long actualBytes;

    @Column(name = "duration_seconds")
    private Double durationSeconds;

    @Column(name = "cloudinary_version")
    private Long cloudinaryVersion;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
