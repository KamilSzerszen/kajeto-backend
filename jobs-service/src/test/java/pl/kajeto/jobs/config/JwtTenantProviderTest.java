package pl.kajeto.jobs.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import pl.kajeto.jobs.domain.TenantId;
import pl.kajeto.jobs.domain.TenantNotResolvedException;

class JwtTenantProviderTest {

  private final JwtTenantProvider provider = new JwtTenantProvider();

  @AfterEach
  void clearContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void returnsTenantFromSubClaim() {
    UUID sub = UUID.randomUUID();
    authenticate(jwtBuilder().subject(sub.toString()).build());

    assertThat(provider.currentTenant()).isEqualTo(new TenantId(sub));
  }

  @Test
  void failsWhenNobodyIsAuthenticated() {
    assertThatThrownBy(provider::currentTenant).isInstanceOf(TenantNotResolvedException.class);
  }

  @Test
  void failsWhenAuthenticationIsNotAJwt() {
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken("user", "pw"));

    assertThatThrownBy(provider::currentTenant).isInstanceOf(TenantNotResolvedException.class);
  }

  @Test
  void failsWhenTokenHasNoSubject() {
    authenticate(jwtBuilder().build());

    assertThatThrownBy(provider::currentTenant)
        .isInstanceOf(TenantNotResolvedException.class)
        .hasMessageContaining("no sub");
  }

  @Test
  void failsWhenSubjectIsNotAUuid() {
    authenticate(jwtBuilder().subject("not-a-uuid").build());

    assertThatThrownBy(provider::currentTenant)
        .isInstanceOf(TenantNotResolvedException.class)
        .hasMessageContaining("not a UUID");
  }

  @Test
  void tenantIdRejectsNull() {
    assertThatThrownBy(() -> new TenantId(null)).isInstanceOf(NullPointerException.class);
  }

  private static Jwt.Builder jwtBuilder() {
    return Jwt.withTokenValue("token").header("alg", "none").claim("scope", "test");
  }

  private static void authenticate(Jwt jwt) {
    SecurityContextHolder.getContext()
        .setAuthentication(new JwtAuthenticationToken(jwt, List.of()));
  }
}
