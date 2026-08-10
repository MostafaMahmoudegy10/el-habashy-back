create table listings (
    id bigserial primary key,
    slug varchar(180) not null unique,
    sector_code varchar(50) not null references sectors(code),
    status varchar(30) not null,
    title_ar text not null,
    title_en text not null,
    summary_ar text not null,
    summary_en text not null,
    description_ar text not null,
    description_en text not null,
    city_ar text not null,
    city_en text not null,
    location_ar text not null,
    location_en text not null,
    price_label_ar text not null,
    price_label_en text not null,
    measure_label varchar(255) not null,
    featured boolean not null default false,
    publish_date date,
    expire_date date,
    auction_date date,
    auction_time time,
    beneficiary_ar text,
    beneficiary_en text,
    venue_ar text,
    venue_en text,
    announcement_source_ar text,
    announcement_source_en text,
    notes_ar text,
    notes_en text,
    map_url text,
    whatsapp_phone varchar(40),
    seo_title_ar text,
    seo_title_en text,
    seo_description_ar text,
    seo_description_en text,
    seo_keywords_ar text,
    seo_keywords_en text,
    views bigint not null default 0,
    whatsapp_clicks bigint not null default 0,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    constraint chk_listings_status check (status in ('ACTIVE', 'INACTIVE', 'CLOSED', 'COMING_SOON')),
    constraint chk_listings_views_non_negative check (views >= 0),
    constraint chk_listings_whatsapp_clicks_non_negative check (whatsapp_clicks >= 0),
    constraint chk_listings_date_range check (expire_date is null or publish_date is null or expire_date >= publish_date)
);

create index idx_listings_sector on listings(sector_code);
create index idx_listings_status on listings(status);
create index idx_listings_featured on listings(featured);
create index idx_listings_created_at on listings(created_at desc);
create index idx_listings_auction_date on listings(auction_date);

create table listing_images (
    id bigserial primary key,
    listing_id bigint not null references listings(id) on delete cascade,
    image_url text not null,
    display_order integer not null,
    constraint chk_listing_images_order_non_negative check (display_order >= 0)
);

create index idx_listing_images_listing on listing_images(listing_id);

create table listing_specifications (
    id bigserial primary key,
    listing_id bigint not null references listings(id) on delete cascade,
    label_ar text not null,
    label_en text not null,
    value_ar text not null,
    value_en text not null,
    display_order integer not null,
    constraint chk_listing_specifications_order_non_negative check (display_order >= 0)
);

create index idx_listing_specifications_listing on listing_specifications(listing_id);
