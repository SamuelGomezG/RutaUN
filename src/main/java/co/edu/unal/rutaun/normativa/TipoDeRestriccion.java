package co.edu.unal.rutaun.normativa;

/**
 * Clase de restricción que muestra UC05-HU01 (glosario, «Restricción (de una materia)»). Las reglas
 * de habilitación quedan fuera de esta historia.
 */
public enum TipoDeRestriccion {
  /** Materias que deben estar aprobadas, o en curso para efectos del plan, antes de cursarla. */
  PRERREQUISITO(
      "Prerrequisito",
      "Materias que deben estar aprobadas, o en curso para efectos del plan, antes de cursar la"
          + " materia"),
  /** Materias que deben cursarse en el mismo semestre que la materia o antes. */
  CORREQUISITO(
      "Correquisito", "Materias que deben cursarse en el mismo semestre que la materia o antes");

  private final String nombre;
  private final String definicion;

  TipoDeRestriccion(String nombre, String definicion) {
    this.nombre = nombre;
    this.definicion = definicion;
  }

  /** El nombre de la clase de restricción, con el vocabulario del glosario. */
  public String nombre() {
    return nombre;
  }

  /**
   * Qué es esta clase de restricción (UC05 paso 4), según el glosario: el prerrequisito pide las
   * materias aprobadas antes; el correquisito, cursadas en el mismo semestre o antes. Se muestra
   * junto a {@link ExpresionVista#redaccion()}, que dice cuáles son esas materias.
   */
  public String definicion() {
    return definicion;
  }
}
