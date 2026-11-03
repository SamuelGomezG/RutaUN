package co.edu.unal.rutaun.normativa.dominio;

import co.edu.unal.rutaun.compartido.Momento;
import co.edu.unal.rutaun.compartido.ReglaId;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.util.Optional;

/**
 * Regla que otra modifica, deroga o exceptúa (glosario, «Regla»). El destino es una regla concreta
 * o todas las reglas de un artículo o numeral de una norma; el momento desde el que surte efecto es
 * opcional.
 *
 * @param tipo relación con la regla afectada
 * @param reglaAfectada regla concreta afectada; vacío si se afecta un artículo completo
 * @param articuloAfectado norma y artículo o numeral cuyas reglas se afectan; vacío si se afecta
 *     una regla
 * @param desde momento desde el que surte efecto; vacío si la norma no lo fija
 */
public record Afectacion(
    TipoDeAfectacion tipo,
    Optional<ReglaId> reglaAfectada,
    Optional<Fuente> articuloAfectado,
    Optional<Momento> desde) {

  /** Exige el tipo y exactamente un destino; valida los optional no nulos. */
  public Afectacion {
    ArgumentChecks.obligatorio(tipo, "tipo");
    ArgumentChecks.obligatorio(reglaAfectada, "reglaAfectada");
    ArgumentChecks.obligatorio(articuloAfectado, "articuloAfectado");
    ArgumentChecks.obligatorio(desde, "desde");
    if (reglaAfectada.isPresent() == articuloAfectado.isPresent()) {
      throw new IllegalArgumentException(
          "la afectación debe indicar exactamente una regla o un artículo");
    }
  }
}
