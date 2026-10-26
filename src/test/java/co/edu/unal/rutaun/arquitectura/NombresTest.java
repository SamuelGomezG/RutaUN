package co.edu.unal.rutaun.arquitectura;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

/** Convenciones de nombres del proyecto (AGENTS.md, «Convenciones de código»). */
class NombresTest {

  private final JavaClasses clases =
      new ClassFileImporter()
          .withImportOption(new ImportOption.DoNotIncludeTests())
          .importPackages("co.edu.unal.rutaun");

  @Test
  void tt01_ningunaClaseTerminaEnImpl() {
    noClasses().should().haveSimpleNameEndingWith("Impl").check(clases);
  }

  @Test
  void tt01_losIdentificadoresSonAscii() {
    // Para las clases, el nombre que ArchUnit evalúa es el completo:
    // el patrón cubre todo ASCII imprimible (letras, dígitos, ., _, $).
    classes()
        .should()
        .haveNameMatching("[\\x21-\\x7E]+")
        .because("el proyecto pide identificadores solo ASCII")
        .check(clases);

    methods()
        .should()
        .haveNameMatching("[a-zA-Z0-9_$]+")
        .because("el proyecto pide identificadores solo ASCII")
        .check(clases);

    fields()
        .should()
        .haveNameMatching("[a-zA-Z0-9_$]+")
        .because("el proyecto pide identificadores solo ASCII")
        .check(clases);
  }
}
