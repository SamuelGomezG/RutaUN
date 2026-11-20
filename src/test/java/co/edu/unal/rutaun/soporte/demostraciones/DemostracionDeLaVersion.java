package co.edu.unal.rutaun.soporte.demostraciones;

import co.edu.unal.rutaun.compartido.Creditos;
import co.edu.unal.rutaun.compartido.EstadoRegla;
import co.edu.unal.rutaun.compartido.NormaId;
import co.edu.unal.rutaun.compartido.ProgramaId;
import co.edu.unal.rutaun.normativa.ClasificacionRegla;
import co.edu.unal.rutaun.normativa.dominio.ConsolidadoValidado;
import co.edu.unal.rutaun.normativa.dominio.DatosDeRegla;
import co.edu.unal.rutaun.normativa.dominio.Fuente;
import co.edu.unal.rutaun.normativa.dominio.MotivoDeVersion;
import co.edu.unal.rutaun.normativa.dominio.Regla;
import co.edu.unal.rutaun.normativa.dominio.ReglaEnVersion;
import co.edu.unal.rutaun.normativa.dominio.VersionDelConsolidado;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Demostración ejecutable de TT-02-T8 (#152): la misma regla tiene un estado en cada versión del
 * consolidado sin reconstituirla. Imprime, por versión, su número, su versión del plan, su motivo y
 * el estado de la regla; al final, si las dos versiones comparten la misma regla. No necesita
 * Spring ni base de datos; se ejecuta con {@code ./mvnw -q test-compile && java -cp
 * target/classes:target/test-classes
 * co.edu.unal.rutaun.soporte.demostraciones.DemostracionDeLaVersion}.
 */
public final class DemostracionDeLaVersion {

  private DemostracionDeLaVersion() {}

  /** Imprime la demostración para quien revisa el PR. */
  public static void main(String[] args) {
    ProgramaId programa = ProgramaId.nuevo();
    NormaId norma = NormaId.nuevo();
    LocalDate fecha = LocalDate.of(2026, 11, 20);
    Regla regla =
        Regla.registrar(
            "Créditos del plan de estudios",
            "El plan de estudios exige 160 créditos.",
            new Fuente(norma, "art. 1"),
            Optional.empty(),
            ClasificacionRegla.DEL_PLAN,
            Optional.empty(),
            new DatosDeRegla.DePrograma(new Creditos(160)));

    VersionDelConsolidado v1 =
        VersionDelConsolidado.primera(
            ConsolidadoValidado.paraImportacion(
                programa, List.of(new ReglaEnVersion(regla, EstadoRegla.PROGRAMADA))),
            new MotivoDeVersion.PorIncorporacion(norma, "Demostración"),
            fecha);
    VersionDelConsolidado v2 =
        v1.siguiente(
            ConsolidadoValidado.paraImportacion(
                programa, List.of(new ReglaEnVersion(regla, EstadoRegla.VIGENTE))),
            new MotivoDeVersion.AutomaticoPorFecha(fecha.plusMonths(1)),
            fecha.plusMonths(1));

    imprimir(v1);
    imprimir(v2);

    Regla enV1 = v1.reglas().get(0).regla();
    Regla enV2 = v2.reglas().get(0).regla();
    System.out.println();
    System.out.println("¿Es la misma regla en las dos versiones? " + (enV1 == enV2));
    System.out.println("¿Son iguales por identidad de regla? " + enV1.equals(enV2));
  }

  private static void imprimir(VersionDelConsolidado version) {
    System.out.println();
    System.out.println(
        "Versión "
            + version.numero()
            + " · versión del plan "
            + version.versionDelPlan()
            + " · motivo "
            + version.motivo().getClass().getSimpleName());
    for (ReglaEnVersion enVersion : version.reglas()) {
      System.out.println(
          "  "
              + enVersion.regla().objeto()
              + " ["
              + idAbreviado(enVersion.regla())
              + "] · "
              + enVersion.estado());
    }
  }

  private static String idAbreviado(Regla regla) {
    return regla.id().valor().toString().substring(0, 8);
  }
}
