# Admin Dashboard API Plan

This plan mirrors the current React dashboard in `el-habashy-front`. Each delivery must be independently testable in Postman. Work pauses after every delivery until it is approved.

## Delivery rules

- Every database change is made through a Flyway migration.
- Every large or required reference dataset gets an idempotent seeder.
- Every endpoint gets integration coverage for success, validation, authentication, and authorization.
- Public reads and admin writes are separated under `/api/v1/public/**` and `/api/v1/admin/**`.
- The frontend is connected domain-by-domain only after the matching API is approved in Postman.

## 0. Admin access bootstrap

- [x] Seed the required enabled administrator account.
- [x] Store the password as a BCrypt hash.
- [x] Keep credentials configurable through environment variables.
- [x] Verify login and access to an admin-only endpoint.

## 1. Sectors

Seed the six dashboard sectors: real estate, movables, cars, antiques, scrap, and other.

- [x] `GET /api/v1/public/sectors`
- [x] `PATCH /api/v1/admin/sectors/{code}`
- [x] Add the database migration, idempotent seed data, validation, and authorization coverage.

The frontend connection remains pending until the API is approved in Postman.

## 2. Listings and auctions

Add listings, localized content, specifications, images, SEO fields, dates, status, and featured state. Seed realistic listings for pagination and filters.

- `GET /api/v1/public/listings`
- `GET /api/v1/public/listings/{slug}`
- `GET /api/v1/admin/listings`
- `POST /api/v1/admin/listings`
- `PUT /api/v1/admin/listings/{id}`
- `PATCH /api/v1/admin/listings/{id}/status`
- `DELETE /api/v1/admin/listings/{id}`

Media upload will be delivered with this domain before the create/edit forms are connected.

## 3. Listing analytics and dashboard overview

- `POST /api/v1/public/listings/{id}/views`
- `POST /api/v1/public/listings/{id}/whatsapp-clicks`
- `GET /api/v1/admin/dashboard/overview`

The overview response will include totals, the most viewed listing, the most contacted listing, and top WhatsApp listings.

## 4. About content

Seed the existing profile, organization structure, certificates, and previous-work categories.

- `GET /api/v1/public/about`
- `PATCH /api/v1/admin/about/profile`
- `PATCH /api/v1/admin/about/structure`
- Certificate CRUD under `/api/v1/admin/about/certificates`
- Previous-work category CRUD under `/api/v1/admin/about/work-categories`

## 5. Services content

Seed arbitration, valuation, and consulting content.

- `GET /api/v1/public/services`
- `GET /api/v1/public/services/{id}`
- Service CRUD under `/api/v1/admin/services`

## 6. Site settings

Seed the current contact, WhatsApp, location, and social settings as a singleton record.

- `GET /api/v1/public/settings`
- `GET /api/v1/admin/settings`
- `PUT /api/v1/admin/settings`

## 7. User administration hardening

The core endpoints already exist. This delivery adds search/filter/sort, pagination validation, and protection against an administrator disabling or demoting their own account.

- `GET /api/v1/admin/users`
- `PATCH /api/v1/admin/users/{id}/role`
- `PATCH /api/v1/admin/users/{id}/status`

## 8. Final frontend cleanup

- Remove domain data from `localStorage` and static seed arrays.
- Add loading, empty, validation, and API-error states to every dashboard panel.
- Run a complete admin workflow against the deployed backend.
