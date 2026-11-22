package pl.kajeto.jobs.architecture;

import static com.tngtech.archunit.core.domain.JavaAccess.Predicates.targetOwner;
import static com.tngtech.archunit.core.domain.JavaCall.Predicates.target;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.assignableTo;
import static com.tngtech.archunit.core.domain.properties.HasName.Predicates.nameMatching;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.lang.ArchRule;
import org.springframework.data.repository.Repository;

/** Rules that keep one tenant away from another tenant's rows. */
final class TenantIsolationRules {

  /** Repository methods that look rows up (or count/delete them) without an owner. */
  static final String UNSCOPED_METHODS =
      "findById|getById|getReferenceById|findAllById|existsById|deleteById|deleteAllById"
          + "|findAll|deleteAll|count";

  static final ArchRule NO_UNSCOPED_REPOSITORY_CALLS =
      noClasses()
          .should()
          .callMethodWhere(
              target(nameMatching(UNSCOPED_METHODS))
                  .and(targetOwner(assignableTo(Repository.class))))
          .as("repositories must be queried with an owner (no findById/findAll/count/...)")
          .allowEmptyShould(true);

  private TenantIsolationRules() {}
}
