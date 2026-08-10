alter table listings
    add column search_vector tsvector generated always as (
        setweight(to_tsvector('simple', coalesce(title_ar, '') || ' ' || coalesce(title_en, '')), 'A') ||
        setweight(to_tsvector('simple', coalesce(summary_ar, '') || ' ' || coalesce(summary_en, '')), 'B') ||
        setweight(to_tsvector('simple',
            coalesce(description_ar, '') || ' ' || coalesce(description_en, '') || ' ' ||
            coalesce(city_ar, '') || ' ' || coalesce(city_en, '') || ' ' ||
            coalesce(location_ar, '') || ' ' || coalesce(location_en, '') || ' ' ||
            coalesce(price_label_ar, '') || ' ' || coalesce(price_label_en, '') || ' ' ||
            coalesce(measure_label, '') || ' ' ||
            coalesce(beneficiary_ar, '') || ' ' || coalesce(beneficiary_en, '') || ' ' ||
            coalesce(venue_ar, '') || ' ' || coalesce(venue_en, '') || ' ' ||
            coalesce(seo_title_ar, '') || ' ' || coalesce(seo_title_en, '') || ' ' ||
            coalesce(seo_keywords_ar, '') || ' ' || coalesce(seo_keywords_en, '')
        ), 'C')
    ) stored;

create index idx_listings_search_vector on listings using gin(search_vector);

create table app_settings (
    id smallint primary key,
    whatsapp_number varchar(40) not null,
    whatsapp_message_ar text not null,
    whatsapp_message_en text not null,
    contact_phone varchar(255) not null,
    contact_email varchar(320) not null,
    office_address_ar text not null,
    office_address_en text not null,
    map_url text not null,
    facebook_url text not null,
    linkedin_url text not null,
    updated_at timestamp with time zone not null default now(),
    constraint chk_app_settings_singleton check (id = 1)
);

insert into app_settings (
    id,
    whatsapp_number,
    whatsapp_message_ar,
    whatsapp_message_en,
    contact_phone,
    contact_email,
    office_address_ar,
    office_address_en,
    map_url,
    facebook_url,
    linkedin_url
) values (
    1,
    '201000000000',
    'أهلا، أحتاج تفاصيل المزاد الخاصة بـ {title}',
    'Hello, I need the auction details for {title}',
    '25789288 - 202 / 25780424 -202 / 25780425 -202',
    'info@elhabashy.com',
    '22 ش محمود بسيوني - قصر النيل - القاهرة',
    '22 Mahmoud Bassiouny St. - Kasr El Nil - Cairo',
    'https://maps.google.com/?q=22%20Mahmoud%20Bassiouny%20St%20Kasr%20El%20Nil%20Cairo',
    'https://www.facebook.com/elhabashy.auctionappraisal/',
    'https://www.linkedin.com/company/elhabashy/'
);
