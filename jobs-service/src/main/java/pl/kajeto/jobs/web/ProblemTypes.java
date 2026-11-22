package pl.kajeto.jobs.web;

import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

/** Stable identifiers and a factory for the RFC 7807 error bodies returned by the API. */
public final class ProblemTypes {

  public static final URI VALIDATION = URI.create("urn:kajeto:problem:validation");
  public static final URI NOT_FOUND = URI.create("urn:kajeto:problem:not-found");
  public static final URI CONFLICT = URI.create("urn:kajeto:problem:conflict");
  public static final URI VERSION_CONFLICT = URI.create("urn:kajeto:problem:version-conflict");
  public static final URI UNAUTHORIZED = URI.create("urn:kajeto:problem:unauthorized");
  public static final URI FORBIDDEN = URI.create("urn:kajeto:problem:forbidden");
  public static final URI INTERNAL = URI.create("urn:kajeto:problem:internal");

  private ProblemTypes() {}

  public static ProblemDetail of(HttpStatus status, URI type, String title, String detail) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
    problem.setType(type);
    problem.setTitle(title);
    return problem;
  }
}
