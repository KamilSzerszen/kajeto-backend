package pl.kajeto.jobs.domain;

/** The current request has no identity we can use as a tenant (missing or malformed token). */
public class TenantNotResolvedException extends RuntimeException {

  public TenantNotResolvedException(String message) {
    super(message);
  }

  public TenantNotResolvedException(String message, Throwable cause) {
    super(message, cause);
  }
}
