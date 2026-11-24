package pl.kajeto.jobs.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;
import pl.kajeto.jobs.domain.TenantProvider;

@AnalyzeClasses(packages = "pl.kajeto.jobs", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

  @ArchTest
  static final ArchRule controllersDoNotUseRepositoriesDirectly =
      noClasses()
          .that()
          .resideInAPackage("pl.kajeto.jobs.web..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("pl.kajeto.jobs.persistence..");

  @ArchTest
  static final ArchRule domainDoesNotDependOnOuterLayers =
      noClasses()
          .that()
          .resideInAPackage("pl.kajeto.jobs.domain..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "pl.kajeto.jobs.web..", "pl.kajeto.jobs.persistence..", "pl.kajeto.jobs.config..");

  @ArchTest
  static final ArchRule persistenceDoesNotDependOnWeb =
      noClasses()
          .that()
          .resideInAPackage("pl.kajeto.jobs.persistence..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("pl.kajeto.jobs.web..");

  @ArchTest
  static final ArchRule restControllersLiveInWebPackage =
      classes()
          .that()
          .areAnnotatedWith(RestController.class)
          .should()
          .resideInAPackage("pl.kajeto.jobs.web..");

  @ArchTest
  static final ArchRule controllersAreNamedController =
      classes()
          .that()
          .resideInAPackage("pl.kajeto.jobs.web..")
          .and()
          .areAnnotatedWith(RestController.class)
          .should()
          .haveSimpleNameEndingWith("Controller");

  @ArchTest
  static final ArchRule layersAreRespected =
      layeredArchitecture()
          .consideringOnlyDependenciesInLayers()
          .withOptionalLayers(true)
          .layer("Web")
          .definedBy("pl.kajeto.jobs.web..")
          .layer("Service")
          .definedBy("pl.kajeto.jobs.service..")
          .layer("Persistence")
          .definedBy("pl.kajeto.jobs.persistence..")
          .whereLayer("Web")
          .mayNotBeAccessedByAnyLayer()
          .whereLayer("Service")
          .mayOnlyBeAccessedByLayers("Web")
          .whereLayer("Persistence")
          .mayOnlyBeAccessedByLayers("Service");

  @ArchTest
  static final ArchRule controllersDoNotResolveTheTenant =
      noClasses()
          .that()
          .resideInAPackage("pl.kajeto.jobs.web..")
          .should()
          .dependOnClassesThat()
          .areAssignableTo(TenantProvider.class);

  @ArchTest
  static final ArchRule servicesLiveInServicePackage =
      classes()
          .that()
          .areAnnotatedWith(Service.class)
          .should()
          .resideInAPackage("pl.kajeto.jobs.service..")
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule noUnscopedRepositoryCalls =
      TenantIsolationRules.NO_UNSCOPED_REPOSITORY_CALLS;
}
