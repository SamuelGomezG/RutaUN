package co.edu.unal.rutaun.normativa.dominio;

import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.util.List;

/**
 * Resultado de validar las reglas nuevas de una incorporación (diseño de clases,
 * «ConsolidadoValidado»). Si no hay motivos trae el {@link ConsolidadoValidado} que solo el
 * validador puede construir; si los hay, trae todos los motivos para que el administrador los
 * corrija juntos.
 */
public sealed interface Validacion permits Validacion.Valido, Validacion.Invalido {

  /**
   * Las reglas pasaron la validación.
   *
   * @param consolidado consolidado listo para crear una versión
   */
  record Valido(ConsolidadoValidado consolidado) implements Validacion {

    /** Exige el consolidado. */
    public Valido {
      ArgumentChecks.obligatorio(consolidado, "consolidado");
    }
  }

  /**
   * Las reglas no pasaron la validación.
   *
   * @param motivos motivos de rechazo, al menos uno
   */
  record Invalido(List<String> motivos) implements Validacion {

    /** Exige al menos un motivo y deja la lista inmutable. */
    public Invalido {
      motivos = ArgumentChecks.copiaSinNulos(motivos, "motivos");
      if (motivos.isEmpty()) {
        throw new IllegalArgumentException("motivos no puede ser vacío");
      }
    }
  }
}
