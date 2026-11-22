package co.edu.unal.rutaun.normativa.persistencia;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Forma en que se guarda el detalle del motivo de una versión del consolidado, como JSON en {@code
 * version_consolidado.detalle_motivo}. La columna {@code motivo} dice la variante, así que el JSON
 * no lleva discriminante.
 */
public sealed interface DetalleDeMotivoJson {

  /** Versión del esquema de este JSON. */
  int ESQUEMA = 1;

  /**
   * Versión del esquema con que se guardó.
   *
   * @return la versión del esquema
   */
  int esquema();

  /**
   * Detalle de una versión por incorporación de una norma.
   *
   * @param esquema versión del esquema
   * @param norma identificador de la norma incorporada
   * @param administrador nombre de quien incorporó la norma
   */
  record PorIncorporacion(int esquema, UUID norma, String administrador)
      implements DetalleDeMotivoJson {}

  /**
   * Detalle de una versión por corrección.
   *
   * @param esquema versión del esquema
   * @param administrador nombre de quien corrigió
   */
  record PorCorreccion(int esquema, String administrador) implements DetalleDeMotivoJson {}

  /**
   * Detalle de una versión automática por periodo.
   *
   * @param esquema versión del esquema
   * @param anio año del periodo
   * @param parte primera o segunda parte del año
   */
  record AutomaticoPorPeriodo(int esquema, int anio, String parte) implements DetalleDeMotivoJson {}

  /**
   * Detalle de una versión automática por fecha.
   *
   * @param esquema versión del esquema
   * @param fecha fecha de la versión
   */
  record AutomaticoPorFecha(int esquema, LocalDate fecha) implements DetalleDeMotivoJson {}
}
