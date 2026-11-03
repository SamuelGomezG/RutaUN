package co.edu.unal.rutaun.normativa.dominio;

/**
 * Relación de una regla con otra: la modifica, la deroga o la exceptúa (glosario, «Regla»; modelo
 * de dominio, «Regla»).
 */
public enum TipoDeAfectacion {
  /** Cambia el contenido de otra regla. */
  MODIFICA,
  /** Deja sin efecto otra regla. */
  DEROGA,
  /** Exceptúa otra regla sin reemplazarla. */
  EXCEPTUA
}
