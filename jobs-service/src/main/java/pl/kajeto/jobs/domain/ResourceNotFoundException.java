package pl.kajeto.jobs.domain;

/**
 * The resource does not exist <b>for the current tenant</b>. Deliberately the same answer for "no
 * such id" and "belongs to someone else", so the API never reveals that a foreign id exists.
 */
public class ResourceNotFoundException extends RuntimeException {

  public ResourceNotFoundException(String message) {
    super(message);
  }
}
