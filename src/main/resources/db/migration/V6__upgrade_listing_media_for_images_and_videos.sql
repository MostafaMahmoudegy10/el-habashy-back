alter table listing_images rename to listing_media;
alter table listing_media rename column image_url to media_url;
alter table listing_media alter column media_url drop not null;

alter table listing_media add column media_type varchar(20) not null default 'IMAGE';
alter table listing_media add column media_role varchar(20) not null default 'GALLERY';
alter table listing_media add column upload_status varchar(20) not null default 'READY';
alter table listing_media add column file_name varchar(255);
alter table listing_media add column content_type varchar(120);
alter table listing_media add column expected_bytes bigint not null default 0;
alter table listing_media add column public_id varchar(300);
alter table listing_media add column format varchar(50);
alter table listing_media add column width integer;
alter table listing_media add column height integer;
alter table listing_media add column actual_bytes bigint;
alter table listing_media add column duration_seconds double precision;
alter table listing_media add column cloudinary_version bigint;
alter table listing_media add column failure_reason varchar(500);
alter table listing_media add column created_at timestamp with time zone not null default current_timestamp;
alter table listing_media add column updated_at timestamp with time zone not null default current_timestamp;

update listing_media
set media_role = case when display_order = 0 then 'THUMBNAIL' else 'GALLERY' end,
    file_name = 'seeded-image-' || id,
    content_type = 'image/remote';

alter table listing_media
    rename constraint chk_listing_images_order_non_negative to chk_listing_media_order_non_negative;

alter table listing_media
    add constraint uk_listing_media_public_id unique (public_id),
    add constraint chk_listing_media_type check (media_type in ('IMAGE', 'VIDEO')),
    add constraint chk_listing_media_role check (media_role in ('THUMBNAIL', 'GALLERY', 'VIDEO')),
    add constraint chk_listing_media_status check (upload_status in ('UPLOADING', 'PROCESSING', 'READY', 'FAILED')),
    add constraint chk_listing_media_expected_bytes check (expected_bytes >= 0),
    add constraint chk_listing_media_actual_bytes check (actual_bytes is null or actual_bytes >= 0),
    add constraint chk_listing_media_role_type check (
        (media_type = 'IMAGE' and media_role in ('THUMBNAIL', 'GALLERY'))
        or (media_type = 'VIDEO' and media_role = 'VIDEO')
    ),
    add constraint chk_listing_media_ready_url check (upload_status <> 'READY' or media_url is not null);

alter index idx_listing_images_listing rename to idx_listing_media_listing;
create index idx_listing_media_status on listing_media(upload_status);
create index idx_listing_media_role on listing_media(listing_id, media_role, display_order);
