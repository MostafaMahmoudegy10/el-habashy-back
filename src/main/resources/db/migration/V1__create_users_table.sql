create table users (
    id uuid primary key,
    first_name varchar(255) not null,
    last_name varchar(255) not null,
    email varchar(255) not null unique,
    password varchar(255) not null,
    enabled boolean not null default false,
    role varchar(20) not null,
    creates_at timestamp not null
);
