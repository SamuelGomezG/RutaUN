package co.edu.unal.rutaun.normativa.aplicacion;

import co.edu.unal.rutaun.normativa.TipoDeRestriccion;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.UUID;

/**
 * Orden en que se muestran las restricciones de una materia (UC05 paso 4): primero los
 * prerrequisitos, luego los correquisitos y al final las reglas de habilitación; dentro de cada
 * clase, por fecha de expedición de la norma (la más antigua primero; sin norma, al final), por
 * artículo, con los números como números, y por identificador de la regla. El mismo dato produce
 * siempre la misma vista.
 */
final class OrdenDeRestricciones {

  private OrdenDeRestricciones() {}

  /**
   * Lo que decide el lugar de una restricción.
   *
   * @param tipo clase de restricción
   * @param expedicion fecha de expedición de la norma de la regla; puede faltar
   * @param articulo artículo de la fuente de la regla
   * @param regla identificador de la regla
   */
  record Clave(TipoDeRestriccion tipo, LocalDate expedicion, String articulo, UUID regla) {

    /** Exige todos los datos, salvo la fecha de expedición. */
    Clave {
      ArgumentChecks.obligatorio(tipo, "tipo");
      ArgumentChecks.obligatorio(articulo, "articulo");
      ArgumentChecks.obligatorio(regla, "regla");
    }
  }

  /** El orden de las restricciones. */
  static final Comparator<Clave> ORDEN =
      Comparator.comparing(Clave::tipo)
          .thenComparing(Clave::expedicion, Comparator.nullsLast(Comparator.naturalOrder()))
          .thenComparing(Clave::articulo, OrdenDeArticulos.INSTANCIA)
          .thenComparing(Clave::regla);
}
