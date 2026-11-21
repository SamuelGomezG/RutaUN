package co.edu.unal.rutaun.normativa;

/**
 * Clase de restricción que muestra UC05-HU01 (glosario, «Restricción (de una materia)»). Las reglas
 * de habilitación quedan fuera de esta historia.
 */
public enum TipoDeRestriccion {
  /** Materias que deben estar aprobadas o en curso antes de cursar la materia. */
  PRERREQUISITO,
  /** Materias que deben cursarse en el mismo semestre que la materia o antes. */
  CORREQUISITO
}
