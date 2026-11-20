package co.edu.unal.rutaun.arquitectura;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.conditions.ArchConditions;
import jakarta.persistence.Entity;
import jakarta.persistence.Version;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Persistable;

/** Toda entidad JPA le dice a Spring Data si es nueva, para no sobrescribir filas (#140). */
class EntidadesTest {

  private static final DescribedPredicate<JavaClass> PERSISTABLE_O_VERSION =
      DescribedPredicate.describe(
          "implementar Persistable o tener un campo @Version",
          clase ->
              clase.isAssignableTo(Persistable.class)
                  || clase.getFields().stream()
                      .anyMatch(campo -> campo.isAnnotatedWith(Version.class)));

  private final JavaClasses clases =
      new ClassFileImporter()
          .withImportOption(new ImportOption.DoNotIncludeTests())
          .importPackages("co.edu.unal.rutaun");

  @Test
  void tt04_cadaEntidadLeDiceASpringDataSiEsNueva() {
    classes()
        .that()
        .areAnnotatedWith(Entity.class)
        .should(ArchConditions.be(PERSISTABLE_O_VERSION))
        .check(clases);
  }
}
