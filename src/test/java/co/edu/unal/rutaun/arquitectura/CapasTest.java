package co.edu.unal.rutaun.arquitectura;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

/** Las capas de un módulo dependen solo hacia abajo (AGENTS.md, «Capas y módulos»). */
class CapasTest {

  private final JavaClasses clases =
      new ClassFileImporter()
          .withImportOption(new ImportOption.DoNotIncludeTests())
          .importPackages("co.edu.unal.rutaun");

  @Test
  void tt01_elDominioNoConoceNiSpringNiPersistencia() {
    ArchRule regla =
        noClasses()
            .that()
            .resideInAPackage("..dominio..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..persistencia..", "org.springframework..");

    regla.check(clases);
  }

  @Test
  void tt01_laPersistenciaNoConoceClasesDeNegocio() {
    ArchRule regla =
        noClasses()
            .that()
            .resideInAPackage("..persistencia..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..aplicacion..", "..dominio..");

    regla.check(clases);
  }

  @Test
  void tt01_laPresentacionNoTocaNiPersistenciaNiLosRepositorios() {
    // Los repositorios de Spring Data viven bajo org.springframework.data:
    // prohibir el paquete entero cubre la regla «solo aplicacion usa los
    // repositorios» hasta que TT-04 (#51) los cree.
    ArchRule sinPersistenciaNiRepositorios =
        noClasses()
            .that()
            .resideInAPackage("co.edu.unal.rutaun.web..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..persistencia..", "org.springframework.data..");

    sinPersistenciaNiRepositorios.check(clases);
  }
}
