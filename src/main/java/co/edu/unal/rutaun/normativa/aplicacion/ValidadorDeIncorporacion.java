package co.edu.unal.rutaun.normativa.aplicacion;

import co.edu.unal.rutaun.normativa.ClasificacionRegla;
import co.edu.unal.rutaun.normativa.dominio.Regla;
import co.edu.unal.rutaun.normativa.dominio.Vigencia;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.util.ArrayList;
import java.util.List;

/**
 * Valida las reglas de una incorporación después de leerlas. Las reglas sin enunciado, sin fuente o
 * con datos incompletos ya las rechaza {@link IncorporacionJsonReader}; este validador aplica las
 * reglas de temporalidad y clasificación de UC07 ext. 6f y determina si la norma es toda
 * informativa (ext. 5a).
 */
public final class ValidadorDeIncorporacion {

  private ValidadorDeIncorporacion() {}

  /** Valida una lectura, propagando el motivo si el JSON no se pudo leer. */
  public static ResultadoDeValidacion validar(IncorporacionJsonReader.ResultadoDeLectura lectura) {
    ArgumentChecks.obligatorio(lectura, "lectura");
    return switch (lectura) {
      case IncorporacionJsonReader.ResultadoDeLectura.Leida leida -> validar(leida.incorporacion());
      case IncorporacionJsonReader.ResultadoDeLectura.Invalida invalida ->
          new ResultadoDeValidacion.Invalida(List.of(invalida.motivo()));
    };
  }

  /** Valida las reglas de una incorporación ya leída. */
  public static ResultadoDeValidacion validar(IncorporacionLeida incorporacion) {
    ArgumentChecks.obligatorio(incorporacion, "incorporacion");
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
    boolean todasInformativas =
        incorporacion.reglas().stream()
            .allMatch(regla -> regla.clasificacion() == ClasificacionRegla.INFORMATIVA);
    return new ResultadoDeValidacion.Valida(incorporacion, todasInformativas);
  }

  /** Resultado sellado de la validación. */
  public sealed interface ResultadoDeValidacion
      permits ResultadoDeValidacion.Valida, ResultadoDeValidacion.Invalida {

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
  }
}
