package co.edu.unal.rutaun.normativa;

/** Cómo una regla posterior afecta a otra, tal como lo muestra UC06 (glosario, «Regla»). */
public enum TipoDeCambio {
  /** Cambia el contenido de la regla. */
  MODIFICA,
  /** Deja sin efecto la regla. */
  DEROGA,
  /** Exceptúa la regla sin reemplazarla. */
  EXCEPTUA
}
