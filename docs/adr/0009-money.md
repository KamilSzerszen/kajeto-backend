# ADR-0009: Money as NUMERIC(12,2) and BigDecimal

Status: proposed (2026-11-22), finalized in the last PR of stage 1

## Context
The application stores prices, deposits and payments in Polish zloty. Rounding errors in money are not acceptable, and amounts must be readable in SQL and JSON.

## Decision
- Database: `NUMERIC(12,2)` (up to 9 999 999 999,99). Java: `BigDecimal`. JSON: a number with two decimal places.
- A value object `Money` (non-negative, at most two decimal places, always scale 2) rejects `10.001` instead of silently rounding it. A balance that can be negative (overpayment) is a plain `BigDecimal`.
- Currency is PLN and is not stored per row. Multi-currency would need a schema change.
- Never `double` or `float`: binary floating point cannot represent `0.1` exactly.
- Totals (sum of payments, "to collect") are computed by the database with `SUM`/`GROUP BY`; the deposit field counts as a payment: `toPay = agreedPrice - deposit - sum(payments)`.

## Alternatives considered
- **Integer cents (`BIGINT`).** Exact and fast, no scale handling. Rejected: every API and SQL value needs conversion, and amounts are harder to read when debugging.

## Consequences
- Readable data and no conversions at the API boundary.
- A single `Money` type must be used everywhere, otherwise scale mismatches (`2.0` vs `2.00`) creep in; `BigDecimal.equals` compares scale, so `Money` normalizes it.
