create table service_articles (
    id bigserial primary key,
    slug varchar(180) not null unique,
    kind varchar(30) not null,
    title_ar text not null,
    title_en text not null,
    summary_ar text not null,
    summary_en text not null,
    content_ar text not null,
    content_en text not null,
    hero_image_url text not null,
    featured boolean not null default false,
    display_order integer not null default 0,
    seo_title_ar text,
    seo_title_en text,
    seo_description_ar text,
    seo_description_en text,
    seo_keywords_ar text,
    seo_keywords_en text,
    created_at timestamp with time zone not null default now(),
    updated_at timestamp with time zone not null default now(),
    constraint chk_service_articles_kind check (kind in ('ARBITRATION', 'VALUATION', 'CONSULTING')),
    constraint chk_service_articles_display_order check (display_order >= 0)
);

create index idx_service_articles_kind_order
    on service_articles(kind, display_order, id);
create index idx_service_articles_featured
    on service_articles(featured);

create table service_article_gallery (
    id bigserial primary key,
    article_id bigint not null references service_articles(id) on delete cascade,
    image_url text not null,
    display_order integer not null,
    constraint chk_service_article_gallery_order check (display_order >= 0)
);

create index idx_service_article_gallery_article
    on service_article_gallery(article_id, display_order, id);
