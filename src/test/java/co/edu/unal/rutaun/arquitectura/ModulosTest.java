package co.edu.unal.rutaun.arquitectura;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static org.assertj.core.api.Assertions.assertThat;

import co.edu.unal.rutaun.RutaUnApplication;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/** Falla si un módulo usa las entrañas (clases internas) de otro. */
class ModulosTest {

  @Test
  void tt01_losModulosCumplenSusReglas() {
    ApplicationModules.of(RutaUnApplication.class).verify();
  }

  @Test
  void tt02_losModulosDeLosContratosSeReconocen() {
    ApplicationModules modulos = ApplicationModules.of(RutaUnApplication.class);

    assertThat(modulos.stream().map(modulo -> modulo.getIdentifier().toString()))
        .contains("compartido", "puertos", "normativa", "incidentes");
  }

  @Test
  void tt01_contextoDeEvaluacionEsApiDeNormativa() {
    JavaClasses clases =
        new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackages("co.edu.unal.rutaun");

    // En la raíz de normativa, no en un subpaquete: así lo pueden implementar Avance y Plan de
    // carrera sin tocar las entrañas del módulo.
    ArchRule regla =
        classes()
            .that()
            .haveSimpleName("ContextoDeEvaluacion")
            .should()
            .resideInAPackage("co.edu.unal.rutaun.normativa")
            .andShould()
            .beInterfaces();

    regla.check(clases);
  }
}
