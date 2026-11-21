package co.edu.unal.rutaun.soporte.demostraciones;

import co.edu.unal.rutaun.compartido.Creditos;
import co.edu.unal.rutaun.compartido.EstadoRegla;
import co.edu.unal.rutaun.compartido.NormaId;
import co.edu.unal.rutaun.compartido.ProgramaId;
import co.edu.unal.rutaun.normativa.ClasificacionRegla;
import co.edu.unal.rutaun.normativa.dominio.Afectacion;
import co.edu.unal.rutaun.normativa.dominio.ConsolidadoValidado;
import co.edu.unal.rutaun.normativa.dominio.DatosDeRegla;
import co.edu.unal.rutaun.normativa.dominio.Fuente;
import co.edu.unal.rutaun.normativa.dominio.MotivoDeVersion;
import co.edu.unal.rutaun.normativa.dominio.Regla;
import co.edu.unal.rutaun.normativa.dominio.ReglaEnVersion;
import co.edu.unal.rutaun.normativa.dominio.TipoAfectacion;
import co.edu.unal.rutaun.normativa.dominio.VersionDelConsolidado;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Demostración ejecutable de TT-02-T8 (#152) y de la versión del plan (S19, #153). La primera parte
 * muestra que la misma regla tiene un estado en cada versión del consolidado sin reconstituirla. La
 * segunda muestra cuándo sube la versión del plan y cómo rechaza datos imposibles. No necesita
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

    System.out.println();
    System.out.println("Segunda parte: versión del plan (S19)");

    Regla baseEstructura =
        regla(
            "Créditos del plan de estudios",
            new Fuente(norma, "art. 1"),
            new DatosDeRegla.DePrograma(new Creditos(160)),
            Optional.empty());
    Regla reforma =
        regla(
            "Créditos del plan de estudios reformado",
            new Fuente(norma, "art. 2"),
            new DatosDeRegla.DePrograma(new Creditos(170)),
            Optional.empty());

    LocalDate dia = fecha.plusDays(1);
    VersionDelConsolidado r1 =
        VersionDelConsolidado.primera(
            consolidado(programa, new ReglaEnVersion(baseEstructura, EstadoRegla.VIGENTE)),
            new MotivoDeVersion.PorIncorporacion(norma, "Demostración"),
            dia);
    VersionDelConsolidado r2 =
        r1.siguiente(
            consolidado(
                programa,
                new ReglaEnVersion(baseEstructura, EstadoRegla.VIGENTE),
                new ReglaEnVersion(reforma, EstadoRegla.PROGRAMADA)),
            new MotivoDeVersion.PorIncorporacion(NormaId.nuevo(), "Demostración"),
            dia.plusDays(1));
    VersionDelConsolidado r3 =
        r2.siguiente(
            consolidado(
                programa,
                new ReglaEnVersion(baseEstructura, EstadoRegla.VIGENTE),
                new ReglaEnVersion(reforma, EstadoRegla.VIGENTE)),
            new MotivoDeVersion.AutomaticoPorFecha(dia.plusDays(2)),
            dia.plusDays(2));

    imprimirSegundaParte("v1 · norma base", r1);
    imprimirSegundaParte("v2 · reforma programada", r2);
    imprimirSegundaParte("v3 · automática", r3);

    Regla correccion =
        regla(
            "Créditos del plan de estudios corregido",
            new Fuente(norma, "art. 3"),
            new DatosDeRegla.DePrograma(new Creditos(175)),
            Optional.empty());
    VersionDelConsolidado r4 =
        r3.siguiente(
            consolidado(
                programa,
                new ReglaEnVersion(correccion, EstadoRegla.VIGENTE),
                new ReglaEnVersion(reforma, EstadoRegla.VIGENTE)),
            new MotivoDeVersion.PorCorreccion("Demostración"),
            dia.plusDays(3));
    imprimirSegundaParte("v4 · corrección", r4);

    Regla excepcion =
        regla(
            "Excepción sobre los créditos corregidos",
            new Fuente(norma, "art. 4"),
            new DatosDeRegla.DeOtraCategoria(),
            Optional.of(
                new Afectacion(
                    TipoAfectacion.EXCEPTUA,
                    Optional.of(correccion.id()),
                    Optional.empty(),
                    Optional.empty())));
    VersionDelConsolidado r5 =
        r4.siguiente(
            consolidado(
                programa,
                new ReglaEnVersion(correccion, EstadoRegla.VIGENTE),
                new ReglaEnVersion(reforma, EstadoRegla.VIGENTE),
                new ReglaEnVersion(excepcion, EstadoRegla.VIGENTE)),
            new MotivoDeVersion.PorIncorporacion(NormaId.nuevo(), "Demostración"),
            dia.plusDays(4));
    imprimirSegundaParte("v5 · excepción", r5);

    imprimirRechazo(
        "La misma regla dos veces",
        () ->
            VersionDelConsolidado.primera(
                consolidado(
                    programa,
                    new ReglaEnVersion(reforma, EstadoRegla.PROGRAMADA),
                    new ReglaEnVersion(reforma, EstadoRegla.VIGENTE)),
                new MotivoDeVersion.PorIncorporacion(norma, "Demostración"),
                dia));
    imprimirRechazo(
        "Una fecha anterior",
        () ->
            r5.siguiente(
                consolidado(
                    programa,
                    new ReglaEnVersion(correccion, EstadoRegla.VIGENTE),
                    new ReglaEnVersion(reforma, EstadoRegla.VIGENTE),
                    new ReglaEnVersion(excepcion, EstadoRegla.VIGENTE)),
                new MotivoDeVersion.PorIncorporacion(norma, "Demostración"),
                dia));
    imprimirRechazo(
        "Dos programas",
        () ->
            VersionDelConsolidado.vigenteEntre(
                List.of(
                    r1,
                    VersionDelConsolidado.primera(
                        consolidado(
                            ProgramaId.nuevo(),
                            new ReglaEnVersion(baseEstructura, EstadoRegla.VIGENTE)),
                        new MotivoDeVersion.PorIncorporacion(norma, "Demostración"),
                        dia))));
  }

  private static ConsolidadoValidado consolidado(ProgramaId programa, ReglaEnVersion... reglas) {
    return ConsolidadoValidado.paraImportacion(programa, List.of(reglas));
  }

  private static Regla regla(
      String objeto, Fuente fuente, DatosDeRegla datos, Optional<Afectacion> afectacion) {
    return Regla.registrar(
        objeto,
        "Enunciado de la demostración.",
        fuente,
        Optional.empty(),
        ClasificacionRegla.DEL_PLAN,
        afectacion,
        datos);
  }

  private static void imprimirSegundaParte(String etiqueta, VersionDelConsolidado version) {
    System.out.println(etiqueta + " · plan " + version.versionDelPlan());
  }

  private static void imprimirRechazo(String etiqueta, Runnable intento) {
    try {
      intento.run();
      System.out.println(etiqueta + ": aceptada (inesperado)");
    } catch (IllegalArgumentException e) {
      System.out.println(etiqueta + ": rechazada · " + e.getMessage());
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
