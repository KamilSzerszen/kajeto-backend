package pl.kajeto.jobs.domain;

/** The request is valid but clashes with the current state (duplicate id, forbidden transition). */
public class ConflictException extends RuntimeException {

  public ConflictException(String message) {
    super(message);
  }
}
