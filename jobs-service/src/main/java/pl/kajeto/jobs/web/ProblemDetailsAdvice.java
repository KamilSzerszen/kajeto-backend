package pl.kajeto.jobs.web;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import pl.kajeto.jobs.domain.ConflictException;
import pl.kajeto.jobs.domain.ResourceNotFoundException;
import pl.kajeto.jobs.domain.TenantNotResolvedException;

/** One place that turns every failure into an RFC 7807 {@code application/problem+json} body. */
@RestControllerAdvice
public class ProblemDetailsAdvice extends ResponseEntityExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(ProblemDetailsAdvice.class);

  /** One entry of the {@code errors} list in a 400 response. */
  public record FieldError(String field, String message) {}

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    List<FieldError> errors =
        ex.getBindingResult().getFieldErrors().stream()
            .map(e -> new FieldError(e.getField(), e.getDefaultMessage()))
            .toList();
    ProblemDetail problem =
        ProblemTypes.of(
            HttpStatus.BAD_REQUEST,
            ProblemTypes.VALIDATION,
            "Validation failed",
            "One or more fields are invalid");
    problem.setProperty("errors", errors);
    return ResponseEntity.badRequest().body(problem);
  }

  @Override
  protected ResponseEntity<Object> handleHttpMessageNotReadable(
      HttpMessageNotReadableException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    ProblemDetail problem =
        ProblemTypes.of(
            HttpStatus.BAD_REQUEST,
            ProblemTypes.VALIDATION,
            "Malformed request",
            "The request body is missing or is not valid JSON");
    return ResponseEntity.badRequest().body(problem);
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  ResponseEntity<ProblemDetail> notFound(ResourceNotFoundException ex) {
    return respond(
        ProblemTypes.of(
            HttpStatus.NOT_FOUND, ProblemTypes.NOT_FOUND, "Resource not found", ex.getMessage()));
  }

  @ExceptionHandler(ConflictException.class)
  ResponseEntity<ProblemDetail> conflict(ConflictException ex) {
    return respond(
        ProblemTypes.of(HttpStatus.CONFLICT, ProblemTypes.CONFLICT, "Conflict", ex.getMessage()));
  }

  @ExceptionHandler(OptimisticLockingFailureException.class)
  ResponseEntity<ProblemDetail> versionConflict(OptimisticLockingFailureException ex) {
    return respond(
        ProblemTypes.of(
            HttpStatus.CONFLICT,
            ProblemTypes.VERSION_CONFLICT,
            "Version conflict",
            "The resource was changed by someone else. Reload it and try again."));
  }

  @ExceptionHandler(TenantNotResolvedException.class)
  ResponseEntity<ProblemDetail> tenantNotResolved(TenantNotResolvedException ex) {
    return respond(
        ProblemTypes.of(
            HttpStatus.UNAUTHORIZED,
            ProblemTypes.UNAUTHORIZED,
            "Unauthorized",
            "Authentication is required"));
  }

  /**
   * Thrown by {@code @PreAuthorize} inside the controller layer, so the filter chain never sees it.
   */
  @ExceptionHandler(AccessDeniedException.class)
  ResponseEntity<ProblemDetail> accessDenied(AccessDeniedException ex) {
    return respond(
        ProblemTypes.of(
            HttpStatus.FORBIDDEN,
            ProblemTypes.FORBIDDEN,
            "Forbidden",
            "You do not have permission to do this"));
  }

  /** Last resort: log the cause, tell the client nothing about internals. */
  @ExceptionHandler(Exception.class)
  ResponseEntity<ProblemDetail> unexpected(Exception ex) {
    log.error("Unexpected error", ex);
    return respond(
        ProblemTypes.of(
            HttpStatus.INTERNAL_SERVER_ERROR,
            ProblemTypes.INTERNAL,
            "Internal error",
            "Something went wrong"));
  }

  private static ResponseEntity<ProblemDetail> respond(ProblemDetail problem) {
    return ResponseEntity.status(problem.getStatus()).body(problem);
  }
}
