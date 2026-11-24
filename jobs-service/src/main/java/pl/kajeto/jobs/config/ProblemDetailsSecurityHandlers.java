package pl.kajeto.jobs.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import pl.kajeto.jobs.web.ProblemTypes;

/**
 * 401 and 403 are produced by the Spring Security filter chain, before any controller advice runs.
 * These handlers give them the same RFC 7807 body as every other error.
 */
public final class ProblemDetailsSecurityHandlers {

  private ProblemDetailsSecurityHandlers() {}

  public static AuthenticationEntryPoint authenticationEntryPoint(ObjectMapper mapper) {
    return (request, response, ex) ->
        write(
            mapper,
            response,
            ProblemTypes.of(
                HttpStatus.UNAUTHORIZED,
                ProblemTypes.UNAUTHORIZED,
                "Unauthorized",
                "Authentication is required"),
            true);
  }

  public static AccessDeniedHandler accessDeniedHandler(ObjectMapper mapper) {
    return (request, response, ex) ->
        write(
            mapper,
            response,
            ProblemTypes.of(
                HttpStatus.FORBIDDEN,
                ProblemTypes.FORBIDDEN,
                "Forbidden",
                "You do not have permission to do this"),
            false);
  }

  private static void write(
      ObjectMapper mapper, HttpServletResponse response, ProblemDetail problem, boolean bearer)
      throws IOException {
    response.setStatus(problem.getStatus());
    response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
    if (bearer) {
      response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
    }
    mapper.writeValue(response.getOutputStream(), problem);
  }
}
