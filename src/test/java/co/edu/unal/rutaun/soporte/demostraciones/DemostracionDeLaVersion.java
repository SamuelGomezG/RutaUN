package co.edu.unal.rutaun.soporte.demostraciones;

import co.edu.unal.rutaun.compartido.Creditos;
import co.edu.unal.rutaun.compartido.EstadoRegla;
import co.edu.unal.rutaun.compartido.Momento;
import co.edu.unal.rutaun.compartido.NormaId;
import co.edu.unal.rutaun.compartido.ProgramaId;
import co.edu.unal.rutaun.normativa.ClasificacionRegla;
import co.edu.unal.rutaun.normativa.dominio.ConsolidadosDePrueba;
import co.edu.unal.rutaun.normativa.dominio.DatosDeRegla;
import co.edu.unal.rutaun.normativa.dominio.Fuente;
import co.edu.unal.rutaun.normativa.dominio.MotivoDeVersion;
import co.edu.unal.rutaun.normativa.dominio.Regla;
import co.edu.unal.rutaun.normativa.dominio.ReglaEnVersion;
import co.edu.unal.rutaun.normativa.dominio.Validacion;
import co.edu.unal.rutaun.normativa.dominio.ValidadorDelConsolidado;
import co.edu.unal.rutaun.normativa.dominio.VersionDelConsolidado;
import co.edu.unal.rutaun.normativa.dominio.Vigencia;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Demostración ejecutable de TT-02-T8 (#152) y TT-02-T9 (#154): la misma regla tiene un estado en
 * cada versión del consolidado sin reconstituirla, y solo el validador del consolidado produce un
 * {@link co.edu.unal.rutaun.normativa.dominio.ConsolidadoValidado}. Imprime, por versión, su
 * número, su versión del plan, su motivo y el estado de la regla; al final, si las dos versiones
 * comparten la misma regla y qué acepta o rechaza el validador. No necesita Spring ni base de
 * datos; se ejecuta con {@code ./mvnw -q test-compile && java -cp
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
            ConsolidadosDePrueba.con(
                programa, List.of(new ReglaEnVersion(regla, EstadoRegla.PROGRAMADA))),
            new MotivoDeVersion.PorIncorporacion(norma, "Demostración"),
            fecha);
    VersionDelConsolidado v2 =
        v1.siguiente(
            ConsolidadosDePrueba.con(
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

    Regla maximo =
        Regla.registrar(
            "Máximo de créditos",
            "El plan admite hasta 20 créditos por periodo.",
            new Fuente(norma, "art. 2"),
            Optional.empty(),
            ClasificacionRegla.DEL_PLAN,
            Optional.empty(),
            new DatosDeRegla.DeOtraCategoria());
    Vigencia permanente = new Vigencia(new Momento.EnFecha(fecha), Optional.empty());

    System.out.println();
    System.out.println("Validador del consolidado");
    imprimirValidacion(
        "Norma permanente",
        ValidadorDelConsolidado.validar(
            programa,
            Optional.of(v2),
            permanente,
            List.of(new ReglaEnVersion(maximo, EstadoRegla.VIGENTE))));
    Vigencia temporal =
        new Vigencia(
            new Momento.EnFecha(fecha), Optional.of(new Momento.EnFecha(fecha.plusYears(1))));
    imprimirValidacion(
        "Norma temporal",
        ValidadorDelConsolidado.validar(
            programa,
            Optional.of(v2),
            temporal,
            List.of(new ReglaEnVersion(maximo, EstadoRegla.VIGENTE))));
    imprimirValidacion(
        "Regla ya en la versión anterior",
        ValidadorDelConsolidado.validar(
            programa,
            Optional.of(v2),
            permanente,
            List.of(new ReglaEnVersion(regla, EstadoRegla.VIGENTE))));
  }

  private static void imprimirValidacion(String etiqueta, Validacion validacion) {
    switch (validacion) {
      case Validacion.Valido valido ->
          System.out.println(
              "  " + etiqueta + ": válido, " + valido.consolidado().reglas().size() + " reglas");
      case Validacion.Invalido invalido ->
          System.out.println("  " + etiqueta + ": no se crea la versión. " + invalido.motivos());
    }
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
