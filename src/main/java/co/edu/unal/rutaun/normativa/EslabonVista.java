package co.edu.unal.rutaun.normativa;

import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Un paso de la cadena de normas de una regla (UC06 ext. 8a): la norma que la expidió o una norma
 * posterior que la modifica, la deroga o la exceptúa.
 *
 * @param cambio cómo afecta a la regla; vacío si es la norma que la expidió
 * @param fuente norma y artículo de este paso
 * @param fechaExpedicion fecha de expedición de la norma, con la que se ordena la cadena
 * @param rige si la regla de este paso es la que rige hoy en el consolidado vigente
 */
public record EslabonVista(
    Optional<TipoDeCambio> cambio, FuenteVista fuente, LocalDate fechaExpedicion, boolean rige) {

  /** Exige todos los datos. */
  public EslabonVista {
    ArgumentChecks.obligatorio(cambio, "cambio");
    ArgumentChecks.obligatorio(fuente, "fuente");
    ArgumentChecks.obligatorio(fechaExpedicion, "fechaExpedicion");
  }
}
