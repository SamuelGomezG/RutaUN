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
  void tt01_elDominioNoConoceNiSpringNiPersistenciaNiSuperiores() {
    ArchRule regla =
        noClasses()
            .that()
            .resideInAPackage("..dominio..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                "..aplicacion..",
                "co.edu.unal.rutaun..web..",
                "..persistencia..",
                "org.springframework..");

    regla.check(clases);
  }

  @Test
  void tt01_laPersistenciaNoConoceClasesDeNegocioNiLaPresentacion() {
    ArchRule regla =
        noClasses()
            .that()
            .resideInAPackage("..persistencia..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("co.edu.unal.rutaun..web..", "..aplicacion..", "..dominio..");

    regla.check(clases);
  }

  @Test
  void tt01_laAplicacionNoDependeDeLaPresentacion() {
    ArchRule regla =
        noClasses()
            .that()
            .resideInAPackage("..aplicacion..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("co.edu.unal.rutaun..web..");

    regla.check(clases);
  }

  @Test
  void tt01_laPresentacionNoTocaNiPersistenciaNiLosRepositorios() {
    // El selector acotado cubre también los web/ de cada módulo (por ejemplo
    // normativa.web) sin matchear paquetes de frameworks. Los repositorios de
    // Spring Data viven bajo org.springframework.data: prohibir el paquete
    // entero cubre la regla «solo aplicacion usa los repositorios» hasta que
    // TT-04 (#51) los cree.
    ArchRule sinPersistenciaNiRepositorios =
        noClasses()
            .that()
            .resideInAPackage("co.edu.unal.rutaun..web..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..persistencia..", "org.springframework.data..");

    sinPersistenciaNiRepositorios.check(clases);
  }
}
