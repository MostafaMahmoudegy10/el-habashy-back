package com.example.elhabashyback.listing.entity;

import com.example.elhabashyback.sector.entity.Sector;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "listings",
        indexes = {
                @Index(name = "idx_listings_sector", columnList = "sector_code"),
                @Index(name = "idx_listings_status", columnList = "status"),
                @Index(name = "idx_listings_featured", columnList = "featured"),
                @Index(name = "idx_listings_created_at", columnList = "created_at"),
                @Index(name = "idx_listings_auction_date", columnList = "auction_date")
        }
)
@Getter
@Setter
public class Listing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 180)
    private String slug;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sector_code", nullable = false)
    private Sector sector;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ListingStatus status;

    @Column(name = "title_ar", nullable = false, columnDefinition = "text")
    private String titleAr;

    @Column(name = "title_en", nullable = false, columnDefinition = "text")
    private String titleEn;

    @Column(name = "summary_ar", nullable = false, columnDefinition = "text")
    private String summaryAr;

    @Column(name = "summary_en", nullable = false, columnDefinition = "text")
    private String summaryEn;

    @Column(name = "description_ar", nullable = false, columnDefinition = "text")
    private String descriptionAr;

    @Column(name = "description_en", nullable = false, columnDefinition = "text")
    private String descriptionEn;

    @Column(name = "city_ar", nullable = false, columnDefinition = "text")
    private String cityAr;

    @Column(name = "city_en", nullable = false, columnDefinition = "text")
    private String cityEn;

    @Column(name = "location_ar", nullable = false, columnDefinition = "text")
    private String locationAr;

    @Column(name = "location_en", nullable = false, columnDefinition = "text")
    private String locationEn;

    @Column(name = "price_label_ar", nullable = false, columnDefinition = "text")
    private String priceLabelAr;

    @Column(name = "price_label_en", nullable = false, columnDefinition = "text")
    private String priceLabelEn;

    @Column(name = "measure_label", nullable = false)
    private String measureLabel;

    @Column(nullable = false)
    private boolean featured;

    @Column(name = "publish_date")
    private LocalDate publishDate;

    @Column(name = "expire_date")
    private LocalDate expireDate;

    @Column(name = "auction_date")
    private LocalDate auctionDate;

    @Column(name = "auction_time")
    private LocalTime auctionTime;

    @Column(name = "beneficiary_ar", columnDefinition = "text")
    private String beneficiaryAr;

    @Column(name = "beneficiary_en", columnDefinition = "text")
    private String beneficiaryEn;

    @Column(name = "venue_ar", columnDefinition = "text")
    private String venueAr;

    @Column(name = "venue_en", columnDefinition = "text")
    private String venueEn;

    @Column(name = "announcement_source_ar", columnDefinition = "text")
    private String announcementSourceAr;

    @Column(name = "announcement_source_en", columnDefinition = "text")
    private String announcementSourceEn;

    @Column(name = "notes_ar", columnDefinition = "text")
    private String notesAr;

    @Column(name = "notes_en", columnDefinition = "text")
    private String notesEn;

    @Column(name = "map_url", columnDefinition = "text")
    private String mapUrl;

    @Column(name = "whatsapp_phone", length = 40)
    private String whatsappPhone;

    @Column(name = "seo_title_ar", columnDefinition = "text")
    private String seoTitleAr;

    @Column(name = "seo_title_en", columnDefinition = "text")
    private String seoTitleEn;

    @Column(name = "seo_description_ar", columnDefinition = "text")
    private String seoDescriptionAr;

    @Column(name = "seo_description_en", columnDefinition = "text")
    private String seoDescriptionEn;

    @Column(name = "seo_keywords_ar", columnDefinition = "text")
    private String seoKeywordsAr;

    @Column(name = "seo_keywords_en", columnDefinition = "text")
    private String seoKeywordsEn;

    @Column(nullable = false)
    private long views;

    @Column(name = "whatsapp_clicks", nullable = false)
    private long whatsappClicks;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Setter(AccessLevel.NONE)
    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<ListingMedia> media = new ArrayList<>();

    @Setter(AccessLevel.NONE)
    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<ListingSpecification> specifications = new ArrayList<>();

    public void replaceSeedImages(List<String> imageUrls) {
        media.clear();
        for (int index = 0; index < imageUrls.size(); index++) {
            ListingMedia image = new ListingMedia();
            image.setListing(this);
            image.setMediaType(MediaType.IMAGE);
            image.setMediaRole(index == 0 ? MediaRole.THUMBNAIL : MediaRole.GALLERY);
            image.setUploadStatus(MediaUploadStatus.READY);
            image.setFileName("seeded-image-" + index);
            image.setContentType("image/remote");
            image.setExpectedBytes(0);
            image.setUploadedBytes(0);
            image.setMediaUrl(imageUrls.get(index));
            image.setDisplayOrder(index);
            media.add(image);
        }
    }

    public void addMedia(ListingMedia item) {
        item.setListing(this);
        media.add(item);
    }

    public void replaceSpecifications(List<ListingSpecification> replacements) {
        specifications.clear();
        for (int index = 0; index < replacements.size(); index++) {
            ListingSpecification specification = replacements.get(index);
            specification.setListing(this);
            specification.setDisplayOrder(index);
            specifications.add(specification);
        }
    }
}
