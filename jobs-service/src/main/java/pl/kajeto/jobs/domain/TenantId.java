package pl.kajeto.jobs.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Identifies the owner of the data (the tenant). Today it is the Keycloak user id (the {@code sub}
 * claim); a company id can replace it later without touching the callers.
 */
public record TenantId(UUID value) {

  public TenantId {
    Objects.requireNonNull(value, "value");
  }
}
