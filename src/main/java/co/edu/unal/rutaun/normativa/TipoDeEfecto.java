package co.edu.unal.rutaun.normativa;

/**
 * Efecto de una norma sobre el consolidado (glosario, «Tipo de efecto»; diseño de clases, «Enums»).
 */
public enum TipoDeEfecto {
  /** Establece reglas del programa, como el acuerdo de su plan de estudios. */
  BASE,
  /** Cambia o deroga reglas de una norma anterior. */
  MODIFICATORIA,
  /**
   * Exceptúa una regla o añade una regla solo durante uno o varios periodos o hasta una fecha. Por
   * eso siempre tiene «vigencia hasta» y sus reglas son informativas.
   */
  EXCEPCION_O_MEDIDA_TEMPORAL;

  /** Si este efecto exige que la norma tenga «vigencia hasta». */
  public boolean exigeVigenciaHasta() {
    return this == EXCEPCION_O_MEDIDA_TEMPORAL;
  }
}
