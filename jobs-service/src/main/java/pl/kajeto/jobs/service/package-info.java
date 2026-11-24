/**
 * Application services: the only layer that resolves the current tenant ({@code TenantProvider}),
 * opens transactions and calls repositories. Controllers call services, never repositories.
 */
package pl.kajeto.jobs.service;
