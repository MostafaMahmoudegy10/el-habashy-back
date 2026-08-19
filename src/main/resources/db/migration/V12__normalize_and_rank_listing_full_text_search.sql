create or replace function normalize_listing_search_text(input_text text)
returns text
language sql
immutable
parallel safe
as $$
    select regexp_replace(
        translate(lower(coalesce(input_text, '')), 'أإآٱىؤئة', 'اااايويه'),
        '[ًٌٍَُِّْـ]',
        '',
        'g'
    );
$$;

create or replace function refresh_listing_search_vector(target_listing_id bigint)
returns void
language plpgsql
set search_path from current
as $$
begin
    update listings listing
       set search_vector =
            setweight(to_tsvector('simple', normalize_listing_search_text(concat_ws(' ',
                listing.title_ar,
                listing.title_en,
                listing.slug
            ))), 'A') ||
            setweight(to_tsvector('simple', normalize_listing_search_text(concat_ws(' ',
                listing.summary_ar,
                listing.summary_en,
                sector.title_ar,
                sector.title_en,
                sector.code
            ))), 'B') ||
            setweight(to_tsvector('simple', normalize_listing_search_text(concat_ws(' ',
                listing.description_ar,
                listing.description_en,
                listing.city_ar,
                listing.city_en,
                listing.location_ar,
                listing.location_en,
                listing.price_label_ar,
                listing.price_label_en,
                listing.measure_label,
                listing.beneficiary_ar,
                listing.beneficiary_en,
                listing.venue_ar,
                listing.venue_en,
                listing.announcement_source_ar,
                listing.announcement_source_en,
                listing.notes_ar,
                listing.notes_en,
                listing.map_url,
                listing.whatsapp_phone,
                listing.seo_title_ar,
                listing.seo_title_en,
                listing.seo_description_ar,
                listing.seo_description_en,
                listing.seo_keywords_ar,
                listing.seo_keywords_en,
                listing.status::text,
                listing.publish_date::text,
                listing.expire_date::text,
                listing.auction_date::text,
                listing.auction_time::text,
                sector.description_ar,
                sector.description_en,
                coalesce((
                    select string_agg(concat_ws(' ',
                        specification.label_ar,
                        specification.label_en,
                        specification.value_ar,
                        specification.value_en
                    ), ' ')
                    from listing_specifications specification
                    where specification.listing_id = listing.id
                ), ''),
                coalesce((
                    select string_agg(concat_ws(' ',
                        media.file_name,
                        media.content_type,
                        media.media_url
                    ), ' ')
                    from listing_media media
                    where media.listing_id = listing.id
                ), '')
            ))), 'C')
      from sectors sector
     where listing.id = target_listing_id
       and sector.code = listing.sector_code;
end;
$$;

create or replace function refresh_listing_search_vector_from_listing()
returns trigger
language plpgsql
set search_path from current
as $$
begin
    perform refresh_listing_search_vector(new.id);
    return new;
end;
$$;

create or replace function refresh_listing_search_vector_from_child()
returns trigger
language plpgsql
set search_path from current
as $$
begin
    if tg_op = 'DELETE' then
        perform refresh_listing_search_vector(old.listing_id);
        return old;
    end if;
    if tg_op = 'UPDATE' and old.listing_id is distinct from new.listing_id then
        perform refresh_listing_search_vector(old.listing_id);
    end if;
    perform refresh_listing_search_vector(new.listing_id);
    return new;
end;
$$;

create or replace function refresh_listing_search_vectors_from_sector()
returns trigger
language plpgsql
set search_path from current
as $$
declare
    listing_record record;
begin
    for listing_record in select id from listings where sector_code = new.code loop
        perform refresh_listing_search_vector(listing_record.id);
    end loop;
    return new;
end;
$$;

do $$
declare
    listing_record record;
begin
    for listing_record in select id from listings loop
        perform refresh_listing_search_vector(listing_record.id);
    end loop;
end;
$$;
