package pl.kajeto.jobs.config;

import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import pl.kajeto.jobs.domain.TenantId;
import pl.kajeto.jobs.domain.TenantNotResolvedException;
import pl.kajeto.jobs.domain.TenantProvider;

/** Resolves the tenant from the {@code sub} claim of the validated Keycloak JWT. */
@Component
public class JwtTenantProvider implements TenantProvider {

  @Override
  public TenantId currentTenant() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication)) {
      throw new TenantNotResolvedException("No JWT authentication in the security context");
    }
    String subject = jwtAuthentication.getToken().getSubject();
    if (subject == null) {
      throw new TenantNotResolvedException("Token has no sub claim");
    }
    try {
      return new TenantId(UUID.fromString(subject));
    } catch (IllegalArgumentException e) {
      throw new TenantNotResolvedException("Token sub claim is not a UUID", e);
    }
  }
}
