# ADR-0008: Multi-tenancy by owner column, enforced in the application

Status: proposed (2026-11-22), finalized in the last PR of stage 1

## Context
Many tradespeople share one application and one database. One must never see or change another's data. Later a company with employees will own the data instead of a single user.

## Decision
- Tenant model: **shared database, shared schema, `owner_id` column** in every business table. `owner_id` is the `sub` claim (a UUID) of the validated Keycloak JWT.
- The current tenant is obtained only through the `TenantProvider` interface (implementation `JwtTenantProvider`). Only the service layer calls it; controllers do not. The owner is never taken from a request parameter or body.
- Every repository query includes `owner_id` (for example `findByIdAndOwnerId...`). Calling `findById`, `findAll`, `count` and similar unscoped methods on a repository is forbidden by an ArchUnit rule, and the rule itself is tested against deliberately bad code.
- A resource of another tenant is answered with **404, not 403**, so the API does not reveal that the id exists. For the query it is simply "no such row".
- Every new resource needs a test "tenant A cannot read, change or delete tenant B's data (404)".
- Role `WORKER` gets 403 everywhere in stage 1; employees are linked to an owner only in stage 5.
- Moving to companies means a new `TenantProvider` implementation and a migration renaming the column; callers do not change.

## Alternatives considered
- **PostgreSQL Row Level Security.** The database itself rejects foreign rows even if a developer forgets a condition. Rejected for now: it needs a session variable set in every transaction (connection pool pitfall), complicates tests and Flyway (migrations need a role bypassing RLS) and is harder to explain than an explicit condition. It may be added later as a second line of defence.
- **Schema per tenant / database per tenant.** Strongest isolation, but migrations and connections multiply with every customer. Not justified for many small tenants.

## Consequences
- Simple model, one schema, cheap to operate and to migrate.
- Isolation depends on discipline: a forgotten `owner_id` leaks data. Mitigated by the ArchUnit rule, by the mandatory "A cannot see B" test per resource and by review checklists.
- Combined with soft delete, queries must also filter `deleted_at` (see ADR-0010).
