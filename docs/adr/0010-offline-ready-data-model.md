# ADR-0010: UUID keys, optimistic locking and soft delete for offline sync

Status: proposed (2026-11-22), finalized in the last PR of stage 1

## Context
The mobile app works without network and synchronizes later (stage 3). The phone must create records offline, two devices may edit the same record, and synchronization must learn what was deleted.

## Decision
- Every table has `id UUID`. The client may supply it on `POST`; if absent the server generates it. A duplicate id answers 409. Entities use a wrapper `Long` version so Spring Data sees a new entity as new (plain INSERT).
- Every table has `version` (JPA `@Version`). An update with a stale version answers **409** (Problem Details) instead of overwriting.
- Every table has `created_at` and `updated_at` (`TIMESTAMPTZ`, `Instant`, UTC).
- Business rows are never physically deleted: `deleted_at` marks them (soft delete), hidden by `@SQLRestriction("deleted_at IS NULL")`. Customers, jobs, payments and shopping items all follow this.
- These columns live in the `BaseEntity` mapped superclass, together with `owner_id` (ADR-0008).
- Migration `V1__baseline.sql` is never edited (Flyway checksums). Changes are new `V2...` files.

## Alternatives considered
- **Sequential (`BIGSERIAL`) ids.** Smaller and faster indexes, but the phone cannot create ids offline without collisions.
- **Hard delete plus a tombstone table.** Cleaner main tables, but two places to keep in sync.

## Consequences
- Offline creation and conflict detection work without redesign in stage 3.
- Random UUIDs make index inserts less local and rows larger than `BIGINT`; UUIDv7 (time-ordered) can be adopted later without schema change.
- Soft delete keeps data (privacy: a real purge is needed for GDPR erasure), complicates unique constraints (use partial indexes with `deleted_at IS NULL`) and requires the deleted filter everywhere (`@SQLRestriction`).
