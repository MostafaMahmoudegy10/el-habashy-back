package com.example.elhabashyback.listing.repository;

import com.example.elhabashyback.listing.entity.Listing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.elhabashyback.listing.entity.ListingStatus;

import java.util.List;
import java.util.Optional;

public interface ListingRepository extends JpaRepository<Listing, Long>, JpaSpecificationExecutor<Listing> {

    Optional<Listing> findBySlugIgnoreCase(String slug);

    boolean existsBySlugIgnoreCase(String slug);

    boolean existsBySlugIgnoreCaseAndIdNot(String slug, Long id);

    @Query(
            value = """
                    select l.*
                    from {h-schema}listings l
                    where (:publicOnly = false or l.status <> 'INACTIVE')
                      and (:category = '' or l.sector_code = :category)
                      and (:status = '' or l.status = :status)
                      and (:featured is null or l.featured = :featured)
                      and (:city = '' or lower(l.city_ar) = lower(:city) or lower(l.city_en) = lower(:city))
                      and l.search_vector @@ to_tsquery('simple', :searchQuery)
                    """,
            countQuery = """
                    select count(*)
                    from {h-schema}listings l
                    where (:publicOnly = false or l.status <> 'INACTIVE')
                      and (:category = '' or l.sector_code = :category)
                      and (:status = '' or l.status = :status)
                      and (:featured is null or l.featured = :featured)
                      and (:city = '' or lower(l.city_ar) = lower(:city) or lower(l.city_en) = lower(:city))
                      and l.search_vector @@ to_tsquery('simple', :searchQuery)
                    """,
            nativeQuery = true
    )
    Page<Listing> searchFullText(
            @Param("category") String category,
            @Param("status") String status,
            @Param("featured") Boolean featured,
            @Param("publicOnly") boolean publicOnly,
            @Param("city") String city,
            @Param("searchQuery") String searchQuery,
            Pageable pageable
    );

    @Query(
            value = """
                    select l.*
                    from {h-schema}listings l
                    where (:publicOnly = false or l.status <> 'INACTIVE')
                      and (:category = '' or l.sector_code = :category)
                      and (:status = '' or l.status = :status)
                      and (:featured is null or l.featured = :featured)
                      and (:city = '' or lower(l.city_ar) = lower(:city) or lower(l.city_en) = lower(:city))
                      and l.search_vector @@ to_tsquery('simple', :searchQuery)
                    order by ts_rank_cd(l.search_vector, to_tsquery('simple', :searchQuery)) desc,
                             l.created_at desc,
                             l.id desc
                    """,
            countQuery = """
                    select count(*)
                    from {h-schema}listings l
                    where (:publicOnly = false or l.status <> 'INACTIVE')
                      and (:category = '' or l.sector_code = :category)
                      and (:status = '' or l.status = :status)
                      and (:featured is null or l.featured = :featured)
                      and (:city = '' or lower(l.city_ar) = lower(:city) or lower(l.city_en) = lower(:city))
                      and l.search_vector @@ to_tsquery('simple', :searchQuery)
                    """,
            nativeQuery = true
    )
    Page<Listing> searchFullTextRanked(
            @Param("category") String category,
            @Param("status") String status,
            @Param("featured") Boolean featured,
            @Param("publicOnly") boolean publicOnly,
            @Param("city") String city,
            @Param("searchQuery") String searchQuery,
            Pageable pageable
    );

    long countByStatus(ListingStatus status);

    long countByStatusNot(ListingStatus status);

    @Query("select coalesce(sum(listing.views), 0) from Listing listing")
    long sumViews();

    @Query("select coalesce(sum(listing.whatsappClicks), 0) from Listing listing")
    long sumWhatsappClicks();

    @Query("select coalesce(sum(listing.views), 0) from Listing listing where listing.status <> :hiddenStatus")
    long sumPublicViews(@Param("hiddenStatus") ListingStatus hiddenStatus);

    @Query("select coalesce(sum(listing.whatsappClicks), 0) from Listing listing where listing.status <> :hiddenStatus")
    long sumPublicWhatsappClicks(@Param("hiddenStatus") ListingStatus hiddenStatus);

    @Query("""
            select distinct listing.cityAr, listing.cityEn
              from Listing listing
             where listing.status <> :hiddenStatus
             order by listing.cityAr, listing.cityEn
            """)
    List<Object[]> findPublicCities(@Param("hiddenStatus") ListingStatus hiddenStatus);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Listing listing
               set listing.views = listing.views + 1
             where lower(listing.slug) = lower(:slug)
               and listing.status <> :hiddenStatus
            """)
    int incrementPublicViews(
            @Param("slug") String slug,
            @Param("hiddenStatus") ListingStatus hiddenStatus
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Listing listing
               set listing.whatsappClicks = listing.whatsappClicks + 1
             where lower(listing.slug) = lower(:slug)
               and listing.status <> :hiddenStatus
            """)
    int incrementPublicWhatsappClicks(
            @Param("slug") String slug,
            @Param("hiddenStatus") ListingStatus hiddenStatus
    );
}
