package co.edu.unal.rutaun.soporte.demostraciones;

import co.edu.unal.rutaun.normativa.TipoNorma;
import co.edu.unal.rutaun.normativa.dominio.Alcance;
import co.edu.unal.rutaun.normativa.dominio.Facultad;
import co.edu.unal.rutaun.normativa.dominio.IdentificacionNorma;
import co.edu.unal.rutaun.normativa.dominio.NivelDeOrgano;
import co.edu.unal.rutaun.normativa.dominio.Organo;
import java.util.Optional;

/**
 * Demostración ejecutable de TT-02-T6 (#149): imprime el órgano con su nivel y su facultad, las dos
 * propuestas de alcance (UC07 ext. 4h; S11), el número normalizado (UC07 ext. 4c) y las dos
 * comparaciones de órganos. No necesita Spring ni base de datos; se ejecuta con {@code ./mvnw -q
 * test-compile && java -cp target/classes:target/test-classes
 * co.edu.unal.rutaun.soporte.demostraciones.DemostracionDeLasNormas}.
 */
public final class DemostracionDeLasNormas {

  private DemostracionDeLasNormas() {}

  /** Imprime la demostración para quien revisa el PR. */
  public static void main(String[] args) {
    Organo consejoDeFacultad =
        new Organo(
            "Consejo de Facultad de Ingeniería",
            NivelDeOrgano.FACULTAD,
            Optional.of(new Facultad("ING")));
    Organo consejoAcademico =
        new Organo("Consejo Académico", NivelDeOrgano.NACIONAL, Optional.empty());

    System.out.println(
        "Órgano: "
            + consejoDeFacultad.nombre()
            + " · nivel "
            + consejoDeFacultad.nivel()
            + " · facultad "
            + consejoDeFacultad.facultad().orElseThrow().codigo());

    Alcance deFacultad = consejoDeFacultad.alcancePropuesto("Los programas de su facultad.");
    System.out.println("Alcance que propone: " + deFacultad.tipo() + " " + deFacultad.facultades());

    Alcance deLaSede = consejoAcademico.alcancePropuesto("Todos los programas de pregrado.");
    System.out.println("Alcance que propone el Consejo Académico: " + deLaSede.tipo());

    IdentificacionNorma identificacion =
        new IdentificacionNorma(TipoNorma.ACUERDO, "003A", 2022, consejoAcademico);
    System.out.println("«003A» queda como: " + identificacion.numero());

    Organo consejoEscritoDistinto =
        new Organo("CONSEJO ACADEMICO", NivelDeOrgano.NACIONAL, Optional.empty());
    System.out.println(
        "¿«CONSEJO ACADEMICO» es el mismo órgano? "
            + consejoAcademico.equals(consejoEscritoDistinto));

    Organo consejoSuperior =
        new Organo("Consejo Superior", NivelDeOrgano.NACIONAL, Optional.empty());
    Organo consejoSuperiorUniversitario =
        new Organo("Consejo Superior Universitario", NivelDeOrgano.NACIONAL, Optional.empty());
    System.out.println(
        "¿«Consejo Superior» es «Consejo Superior Universitario»? "
            + consejoSuperior.equals(consejoSuperiorUniversitario));
  }
}
