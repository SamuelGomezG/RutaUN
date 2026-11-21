package co.edu.unal.rutaun.normativa;

/**
 * Tipo de un componente con materias. El de libre elección no está aquí: se cubre con créditos y
 * tiene su propia clase, {@link co.edu.unal.rutaun.normativa.dominio.ComponenteDeLibreEleccion}.
 */
public enum TipoComponente {
  /** Componente de fundamentación. */
  FUNDAMENTACION,
  /**
   * Componente de formación disciplinar o profesional (Acuerdo 033 de 2007, art. 9), que incluye el
   * trabajo de grado.
   */
  DISCIPLINAR
}
