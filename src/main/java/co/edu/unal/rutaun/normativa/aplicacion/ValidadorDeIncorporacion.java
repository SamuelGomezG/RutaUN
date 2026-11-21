package co.edu.unal.rutaun.normativa.aplicacion;

import co.edu.unal.rutaun.compartido.ReglaId;
import co.edu.unal.rutaun.normativa.ClasificacionRegla;
import co.edu.unal.rutaun.normativa.dominio.Afectacion;
import co.edu.unal.rutaun.normativa.dominio.Regla;
import co.edu.unal.rutaun.normativa.dominio.Vigencia;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Valida las reglas de una incorporación después de leerlas. Las reglas sin enunciado, sin fuente o
 * con datos incompletos ya las rechaza {@link IncorporacionJsonReader}; este validador aplica las
 * reglas de temporalidad y clasificación de UC07 ext. 6f y determina si la norma es toda
 * informativa (ext. 5a).
 */
public final class ValidadorDeIncorporacion {

  private ValidadorDeIncorporacion() {}

  /** Valida una lectura sin comprobar el registro de las reglas afectadas. */
  public static ResultadoDeValidacion validar(IncorporacionJsonReader.ResultadoDeLectura lectura) {
    return validar(lectura, regla -> true);
  }

  /**
   * Valida una lectura, propagando el motivo si el JSON no se pudo leer.
   *
   * @param lectura resultado del lector
   * @param registrada si una regla afectada ya está guardada
   */
  public static ResultadoDeValidacion validar(
      IncorporacionJsonReader.ResultadoDeLectura lectura, Predicate<ReglaId> registrada) {
    ArgumentChecks.obligatorio(lectura, "lectura");
    ArgumentChecks.obligatorio(registrada, "registrada");
    return switch (lectura) {
      case IncorporacionJsonReader.ResultadoDeLectura.Leida leida ->
          validar(leida.incorporacion(), registrada);
      case IncorporacionJsonReader.ResultadoDeLectura.Invalida invalida ->
          new ResultadoDeValidacion.Invalida(List.of(invalida.motivo()));
    };
  }

  /** Valida las reglas de una incorporación sin comprobar el registro de las reglas afectadas. */
  public static ResultadoDeValidacion validar(IncorporacionLeida incorporacion) {
    return validar(incorporacion, regla -> true);
  }

  /**
   * Valida las reglas de una incorporación ya leída.
   *
   * <p>UC07 ext. 6b: si una regla remite a otra que no está registrada, devuelve {@link
   * ResultadoDeValidacion.ReglaAfectadaNoRegistrada}. Mientras el lector no resuelva la regla real
   * (#182), el identificador derivado de su norma, su artículo y su objeto no coincide con una
   * regla guardada.
   *
   * @param incorporacion incorporación leída
   * @param registrada si una regla afectada ya está guardada
   */
  public static ResultadoDeValidacion validar(
      IncorporacionLeida incorporacion, Predicate<ReglaId> registrada) {
    ArgumentChecks.obligatorio(incorporacion, "incorporacion");
    ArgumentChecks.obligatorio(registrada, "registrada");
    List<String> motivos = new ArrayList<>();
    if (incorporacion.reglas().isEmpty()) {
      motivos.add("La incorporación no tiene reglas.");
    }
    boolean normaTemporal = incorporacion.norma().vigencia().esTemporal();
    for (Regla regla : incorporacion.reglas()) {
      boolean temporal =
          normaTemporal || regla.vigenciaPropia().map(Vigencia::esTemporal).orElse(false);
      if (temporal && regla.clasificacion() == ClasificacionRegla.DEL_PLAN) {
        motivos.add(
            "La regla \""
                + regla.objeto()
                + "\" ("
                + regla.fuente().articulo()
                + ") es temporal y está clasificada como DEL_PLAN; debe ser INFORMATIVA.");
      }
    }
    if (!motivos.isEmpty()) {
      return new ResultadoDeValidacion.Invalida(motivos);
    }
    for (Regla regla : incorporacion.reglas()) {
      ReglaId afectada = regla.afectacion().flatMap(Afectacion::reglaAfectada).orElse(null);
      if (afectada != null && !registrada.test(afectada)) {
        return new ResultadoDeValidacion.ReglaAfectadaNoRegistrada(
            regla.objeto(), regla.fuente().articulo());
      }
    }
    boolean todasInformativas =
        incorporacion.reglas().stream()
            .allMatch(regla -> regla.clasificacion() == ClasificacionRegla.INFORMATIVA);
    return new ResultadoDeValidacion.Valida(incorporacion, todasInformativas);
  }

  /** Resultado sellado de la validación. */
  public sealed interface ResultadoDeValidacion
      permits ResultadoDeValidacion.Valida,
          ResultadoDeValidacion.Invalida,
          ResultadoDeValidacion.ReglaAfectadaNoRegistrada {

    /**
     * La incorporación se acepta.
     *
     * @param incorporacion incorporación validada
     * @param ningunPlanQuedaDesactualizado si todas las reglas son informativas
     */
    record Valida(IncorporacionLeida incorporacion, boolean ningunPlanQuedaDesactualizado)
        implements ResultadoDeValidacion {

      /** Exige la incorporación. */
      public Valida {
        ArgumentChecks.obligatorio(incorporacion, "incorporacion");
      }
    }

    /** La incorporación se rechaza con uno o varios motivos. */
    record Invalida(List<String> motivos) implements ResultadoDeValidacion {

      /** Exige al menos un motivo y deja la lista inmutable. */
      public Invalida {
        motivos = ArgumentChecks.copiaSinNulos(motivos, "motivos");
        if (motivos.isEmpty()) {
          throw new IllegalArgumentException("motivos no puede ser vacío");
        }
      }
    }

    /**
     * Una regla remite a otra que no está registrada (UC07 ext. 6b).
     *
     * @param objeto objeto de la regla que afecta
     * @param articulo artículo de la regla que afecta
     */
    record ReglaAfectadaNoRegistrada(String objeto, String articulo)
        implements ResultadoDeValidacion {

      /** Exige el objeto y el artículo de la regla que afecta. */
      public ReglaAfectadaNoRegistrada {
        ArgumentChecks.obligatorioNoVacio(objeto, "objeto");
        ArgumentChecks.obligatorioNoVacio(articulo, "articulo");
      }
    }
  }
}
