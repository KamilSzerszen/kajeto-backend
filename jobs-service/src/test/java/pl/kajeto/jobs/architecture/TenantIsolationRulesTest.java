package pl.kajeto.jobs.architecture;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;
import pl.kajeto.archfixture.CompliantService;
import pl.kajeto.archfixture.ViolatingService;

/** Proves the isolation rule really fails on bad code (a rule that never fails is worthless). */
class TenantIsolationRulesTest {

  private final ClassFileImporter importer = new ClassFileImporter();

  @Test
  void ruleRejectsFindByIdAndCountWithoutOwner() {
    JavaClasses classes = importer.importClasses(ViolatingService.class);

    assertThatThrownBy(() -> TenantIsolationRules.NO_UNSCOPED_REPOSITORY_CALLS.check(classes))
        .isInstanceOf(AssertionError.class)
        .hasMessageContaining("findById")
        .hasMessageContaining("count");
  }

  @Test
  void ruleAcceptsOwnerScopedLookup() {
    JavaClasses classes = importer.importClasses(CompliantService.class);

    assertThatCode(() -> TenantIsolationRules.NO_UNSCOPED_REPOSITORY_CALLS.check(classes))
        .doesNotThrowAnyException();
  }
}
