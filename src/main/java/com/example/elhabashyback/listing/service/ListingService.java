package com.example.elhabashyback.listing.service;

import com.example.elhabashyback.common.dto.LocalizedTextRequest;
import com.example.elhabashyback.common.dto.PageResponse;
import com.example.elhabashyback.common.exception.BadRequestException;
import com.example.elhabashyback.common.exception.ConflictException;
import com.example.elhabashyback.common.exception.ResourceNotFoundException;
import com.example.elhabashyback.configuration.cache.CacheConfiguration;
import com.example.elhabashyback.listing.dto.ListingResponse;
import com.example.elhabashyback.listing.dto.ListingEngagementResponse;
import com.example.elhabashyback.listing.dto.ListingSpecificationRequest;
import com.example.elhabashyback.listing.dto.UpsertListingRequest;
import com.example.elhabashyback.listing.entity.Listing;
import com.example.elhabashyback.listing.entity.ListingSpecification;
import com.example.elhabashyback.listing.entity.ListingStatus;
import com.example.elhabashyback.listing.repository.ListingRepository;
import com.example.elhabashyback.sector.entity.Sector;
import com.example.elhabashyback.sector.repository.SectorRepository;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import javax.sql.DataSource;
import java.sql.SQLException;

@Service
@RequiredArgsConstructor
public class ListingService {

    private static final Map<String, String> SORT_FIELDS = Map.of(
            "createdAt", "createdAt",
            "publishDate", "publishDate",
            "auctionDate", "auctionDate",
            "titleAr", "titleAr",
            "titleEn", "titleEn",
            "views", "views",
            "whatsappClicks", "whatsappClicks"
    );
    private static final Map<String, String> SORT_COLUMNS = Map.of(
            "createdAt", "created_at",
            "publishDate", "publish_date",
            "auctionDate", "auction_date",
            "titleAr", "title_ar",
            "titleEn", "title_en",
            "views", "views",
            "whatsappClicks", "whatsapp_clicks"
    );

    private final ListingRepository listingRepository;
    private final SectorRepository sectorRepository;
    private final DataSource dataSource;
    private volatile Boolean postgresFtsSupported;

    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = CacheConfiguration.PUBLIC_LISTINGS,
            condition = "#p3 == null || #p3.isBlank()",
            sync = true
    )
    public PageResponse<ListingResponse> listPublic(
            String category,
            String status,
            Boolean featured,
            String search,
            int page,
            int size,
            String sort
    ) {
        return list(category, status, featured, search, true, page, size, sort);
    }

    @Transactional(readOnly = true)
    public PageResponse<ListingResponse> listAdmin(
            String category,
            String status,
            Boolean featured,
            String search,
            int page,
            int size,
            String sort
    ) {
        return list(category, status, featured, search, false, page, size, sort);
    }

    @Transactional
    public ListingResponse getPublicBySlug(String slug) {
        String normalizedSlug = normalizeSlug(slug);
        int updated = listingRepository.incrementPublicViews(normalizedSlug, ListingStatus.INACTIVE);
        if (updated == 0) {
            throw new ResourceNotFoundException("Listing not found");
        }
        Listing listing = getPublicListing(normalizedSlug);
        return ListingResponse.fromPublic(listing);
    }

    @Transactional
    public ListingEngagementResponse trackWhatsappClick(String slug) {
        String normalizedSlug = normalizeSlug(slug);
        int updated = listingRepository.incrementPublicWhatsappClicks(normalizedSlug, ListingStatus.INACTIVE);
        if (updated == 0) {
            throw new ResourceNotFoundException("Listing not found");
        }
        return ListingEngagementResponse.from(getPublicListing(normalizedSlug));
    }

    @Transactional
    @CacheEvict(cacheNames = CacheConfiguration.PUBLIC_LISTINGS, allEntries = true)
    public ListingResponse create(UpsertListingRequest request) {
        return ListingResponse.fromAdmin(createEntity(request));
    }

    Listing createEntity(UpsertListingRequest request) {
        validateDates(request.publishDate(), request.expireDate());
        String slug = resolveSlug(request.slug(), request.title().en());
        if (listingRepository.existsBySlugIgnoreCase(slug)) {
            throw new ConflictException("Listing slug already exists");
        }

        Listing listing = new Listing();
        listing.setSlug(slug);
        listing.setViews(0);
        listing.setWhatsappClicks(0);
        apply(listing, request);
        return listingRepository.saveAndFlush(listing);
    }

    @Transactional
    @CacheEvict(cacheNames = CacheConfiguration.PUBLIC_LISTINGS, allEntries = true)
    public ListingResponse update(Long id, UpsertListingRequest request) {
        validateDates(request.publishDate(), request.expireDate());
        Listing listing = get(id);
        String slug = resolveSlug(request.slug(), request.title().en());
        if (listingRepository.existsBySlugIgnoreCaseAndIdNot(slug, id)) {
            throw new ConflictException("Listing slug already exists");
        }

        listing.setSlug(slug);
        apply(listing, request);
        listingRepository.flush();
        return ListingResponse.fromAdmin(listing);
    }

    @Transactional
    @CacheEvict(cacheNames = CacheConfiguration.PUBLIC_LISTINGS, allEntries = true)
    public ListingResponse updateStatus(Long id, ListingStatus status) {
        Listing listing = get(id);
        listing.setStatus(status);
        listingRepository.flush();
        return ListingResponse.fromAdmin(listing);
    }

    @Transactional
    @CacheEvict(cacheNames = CacheConfiguration.PUBLIC_LISTINGS, allEntries = true)
    public void delete(Long id) {
        Listing listing = get(id);
        listingRepository.delete(listing);
        listingRepository.flush();
    }

    private PageResponse<ListingResponse> list(
            String category,
            String statusValue,
            Boolean featured,
            String search,
            boolean publicOnly,
            int page,
            int size,
            String sort
    ) {
        ListingStatus status = parseStatus(statusValue);
        String searchTerm = search == null ? "" : search.trim();
        boolean useFullTextSearch = !searchTerm.isBlank() && supportsPostgresFts();
        Pageable pageable = PageRequest.of(page, size, parseSort(sort, useFullTextSearch));
        Page<Listing> listings = useFullTextSearch
                ? listingRepository.searchFullText(
                        normalizedCategory(category),
                        status == null ? "" : status.name(),
                        featured,
                        publicOnly,
                        searchTerm,
                        pageable
                )
                : listingRepository.findAll(filters(category, status, featured, searchTerm, publicOnly), pageable);
        Page<ListingResponse> result = listings
                .map(listing -> publicOnly
                        ? ListingResponse.fromPublic(listing)
                        : ListingResponse.fromAdmin(listing));
        return PageResponse.from(result);
    }

    private Specification<Listing> filters(
            String category,
            ListingStatus status,
            Boolean featured,
            String search,
            boolean publicOnly
    ) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (publicOnly) {
                predicates.add(builder.notEqual(root.get("status"), ListingStatus.INACTIVE));
            }
            if (category != null && !category.isBlank()) {
                predicates.add(builder.equal(
                        root.get("sector").get("code"),
                        category.trim().toLowerCase(Locale.ROOT)
                ));
            }
            if (status != null) {
                predicates.add(builder.equal(root.get("status"), status));
            }
            if (featured != null) {
                predicates.add(builder.equal(root.get("featured"), featured));
            }
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
                var sector = root.join("sector", JoinType.INNER);
                var specifications = root.join("specifications", JoinType.LEFT);
                var media = root.join("media", JoinType.LEFT);
                query.distinct(true);
                predicates.add(builder.or(
                        builder.like(builder.lower(root.get("slug")), pattern),
                        builder.like(builder.lower(root.get("titleAr")), pattern),
                        builder.like(builder.lower(root.get("titleEn")), pattern),
                        builder.like(builder.lower(root.get("summaryAr")), pattern),
                        builder.like(builder.lower(root.get("summaryEn")), pattern),
                        builder.like(builder.lower(root.get("descriptionAr")), pattern),
                        builder.like(builder.lower(root.get("descriptionEn")), pattern),
                        builder.like(builder.lower(root.get("cityAr")), pattern),
                        builder.like(builder.lower(root.get("cityEn")), pattern),
                        builder.like(builder.lower(root.get("locationAr")), pattern),
                        builder.like(builder.lower(root.get("locationEn")), pattern),
                        builder.like(builder.lower(root.get("priceLabelAr")), pattern),
                        builder.like(builder.lower(root.get("priceLabelEn")), pattern),
                        builder.like(builder.lower(root.get("measureLabel")), pattern),
                        builder.like(builder.lower(root.get("beneficiaryAr")), pattern),
                        builder.like(builder.lower(root.get("beneficiaryEn")), pattern),
                        builder.like(builder.lower(root.get("venueAr")), pattern),
                        builder.like(builder.lower(root.get("venueEn")), pattern),
                        builder.like(builder.lower(root.get("announcementSourceAr")), pattern),
                        builder.like(builder.lower(root.get("announcementSourceEn")), pattern),
                        builder.like(builder.lower(root.get("notesAr")), pattern),
                        builder.like(builder.lower(root.get("notesEn")), pattern),
                        builder.like(builder.lower(root.get("mapUrl")), pattern),
                        builder.like(builder.lower(root.get("whatsappPhone")), pattern),
                        builder.like(builder.lower(root.get("seoTitleAr")), pattern),
                        builder.like(builder.lower(root.get("seoTitleEn")), pattern),
                        builder.like(builder.lower(root.get("seoDescriptionAr")), pattern),
                        builder.like(builder.lower(root.get("seoDescriptionEn")), pattern),
                        builder.like(builder.lower(root.get("seoKeywordsAr")), pattern),
                        builder.like(builder.lower(root.get("seoKeywordsEn")), pattern),
                        builder.like(builder.lower(root.get("status").as(String.class)), pattern),
                        builder.like(builder.lower(root.get("publishDate").as(String.class)), pattern),
                        builder.like(builder.lower(root.get("expireDate").as(String.class)), pattern),
                        builder.like(builder.lower(root.get("auctionDate").as(String.class)), pattern),
                        builder.like(builder.lower(root.get("auctionTime").as(String.class)), pattern),
                        builder.like(builder.lower(sector.get("code")), pattern),
                        builder.like(builder.lower(sector.get("titleAr")), pattern),
                        builder.like(builder.lower(sector.get("titleEn")), pattern),
                        builder.like(builder.lower(sector.get("descriptionAr")), pattern),
                        builder.like(builder.lower(sector.get("descriptionEn")), pattern),
                        builder.like(builder.lower(specifications.get("labelAr")), pattern),
                        builder.like(builder.lower(specifications.get("labelEn")), pattern),
                        builder.like(builder.lower(specifications.get("valueAr")), pattern),
                        builder.like(builder.lower(specifications.get("valueEn")), pattern),
                        builder.like(builder.lower(media.get("fileName")), pattern),
                        builder.like(builder.lower(media.get("contentType")), pattern),
                        builder.like(builder.lower(media.get("mediaUrl")), pattern)
                ));
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Sort parseSort(String sortValue, boolean nativeColumns) {
        String value = sortValue == null || sortValue.isBlank() ? "createdAt,desc" : sortValue.trim();
        String[] parts = value.split(",", 2);
        String field = (nativeColumns ? SORT_COLUMNS : SORT_FIELDS).get(parts[0]);
        if (field == null) {
            throw new BadRequestException("Unsupported listing sort field");
        }
        Sort.Direction direction = parts.length == 1
                ? Sort.Direction.ASC
                : Sort.Direction.fromOptionalString(parts[1]).orElseThrow(
                        () -> new BadRequestException("Sort direction must be asc or desc"));
        return Sort.by(direction, field);
    }

    private String normalizedCategory(String category) {
        return category == null || category.isBlank()
                ? ""
                : category.trim().toLowerCase(Locale.ROOT);
    }

    private boolean supportsPostgresFts() {
        Boolean cached = postgresFtsSupported;
        if (cached != null) {
            return cached;
        }
        try (var connection = dataSource.getConnection()) {
            boolean supported = connection.getMetaData().getDatabaseProductName()
                    .toLowerCase(Locale.ROOT)
                    .contains("postgresql");
            postgresFtsSupported = supported;
            return supported;
        } catch (SQLException exception) {
            postgresFtsSupported = false;
            return false;
        }
    }

    private ListingStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return ListingStatus.fromValue(status.trim());
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException("Unsupported listing status");
        }
    }

    private void apply(Listing listing, UpsertListingRequest request) {
        Sector sector = sectorRepository.findById(request.category().trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new BadRequestException("Unknown listing category"));

        listing.setSector(sector);
        listing.setStatus(request.status());
        listing.setTitleAr(trim(request.title().ar()));
        listing.setTitleEn(trim(request.title().en()));
        listing.setSummaryAr(trim(request.summary().ar()));
        listing.setSummaryEn(trim(request.summary().en()));
        listing.setDescriptionAr(trim(request.description().ar()));
        listing.setDescriptionEn(trim(request.description().en()));
        listing.setCityAr(trim(request.city().ar()));
        listing.setCityEn(trim(request.city().en()));
        listing.setLocationAr(trim(request.location().ar()));
        listing.setLocationEn(trim(request.location().en()));
        listing.setPriceLabelAr(trim(request.priceLabel().ar()));
        listing.setPriceLabelEn(trim(request.priceLabel().en()));
        listing.setMeasureLabel(trim(request.measureLabel()));
        listing.setFeatured(request.featured());
        listing.setPublishDate(request.publishDate());
        listing.setExpireDate(request.expireDate());
        listing.setAuctionDate(request.auctionDate());
        listing.setAuctionTime(request.auctionTime());
        setOptionalText(listing, request.beneficiary(), OptionalTextTarget.BENEFICIARY);
        setOptionalText(listing, request.venue(), OptionalTextTarget.VENUE);
        setOptionalText(listing, request.announcementSource(), OptionalTextTarget.ANNOUNCEMENT_SOURCE);
        setOptionalText(listing, request.notes(), OptionalTextTarget.NOTES);
        setOptionalText(listing, request.seoTitle(), OptionalTextTarget.SEO_TITLE);
        setOptionalText(listing, request.seoDescription(), OptionalTextTarget.SEO_DESCRIPTION);
        setOptionalText(listing, request.seoKeywords(), OptionalTextTarget.SEO_KEYWORDS);
        listing.setMapUrl(nullableTrim(request.mapUrl()));
        listing.setWhatsappPhone(nullableTrim(request.whatsappPhone()));
        listing.replaceSpecifications(request.specs().stream().map(this::toSpecification).toList());
    }

    private ListingSpecification toSpecification(ListingSpecificationRequest request) {
        ListingSpecification specification = new ListingSpecification();
        specification.setLabelAr(trim(request.label().ar()));
        specification.setLabelEn(trim(request.label().en()));
        specification.setValueAr(trim(request.value().ar()));
        specification.setValueEn(trim(request.value().en()));
        return specification;
    }

    private void setOptionalText(Listing listing, LocalizedTextRequest text, OptionalTextTarget target) {
        String ar = text == null ? null : trim(text.ar());
        String en = text == null ? null : trim(text.en());
        switch (target) {
            case BENEFICIARY -> { listing.setBeneficiaryAr(ar); listing.setBeneficiaryEn(en); }
            case VENUE -> { listing.setVenueAr(ar); listing.setVenueEn(en); }
            case ANNOUNCEMENT_SOURCE -> { listing.setAnnouncementSourceAr(ar); listing.setAnnouncementSourceEn(en); }
            case NOTES -> { listing.setNotesAr(ar); listing.setNotesEn(en); }
            case SEO_TITLE -> { listing.setSeoTitleAr(ar); listing.setSeoTitleEn(en); }
            case SEO_DESCRIPTION -> { listing.setSeoDescriptionAr(ar); listing.setSeoDescriptionEn(en); }
            case SEO_KEYWORDS -> { listing.setSeoKeywordsAr(ar); listing.setSeoKeywordsEn(en); }
        }
    }

    private Listing get(Long id) {
        return listingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));
    }

    private Listing getPublicListing(String normalizedSlug) {
        return listingRepository.findBySlugIgnoreCase(normalizedSlug)
                .filter(item -> item.getStatus() != ListingStatus.INACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));
    }

    private void validateDates(LocalDate publishDate, LocalDate expireDate) {
        if (publishDate != null && expireDate != null && expireDate.isBefore(publishDate)) {
            throw new BadRequestException("expireDate cannot be before publishDate");
        }
    }

    private String resolveSlug(String requestedSlug, String englishTitle) {
        String candidate = requestedSlug == null || requestedSlug.isBlank() ? englishTitle : requestedSlug;
        String normalized = normalizeSlug(candidate);
        return normalized.isBlank() ? "listing-" + UUID.randomUUID().toString().substring(0, 8) : normalized;
    }

    private String normalizeSlug(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }

    private String trim(String value) {
        return value.trim();
    }

    private String nullableTrim(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private enum OptionalTextTarget {
        BENEFICIARY,
        VENUE,
        ANNOUNCEMENT_SOURCE,
        NOTES,
        SEO_TITLE,
        SEO_DESCRIPTION,
        SEO_KEYWORDS
    }
}
