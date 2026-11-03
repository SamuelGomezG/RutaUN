package co.edu.unal.rutaun.normativa.dominio;

/**
 * Origen de una materia externa (glosario, «Materia externa»): una materia de nivelación, que solo
 * cursa quien la debe por su puntaje de admisión, o una materia de otro programa, que el SIA no
 * deja inscribir a los estudiantes del programa.
 */
public enum TipoDeMateriaExterna {
  /** Materia de nivelación: se da por cumplida al evaluar un prerrequisito. */
  NIVELACION,
  /** Materia de otro programa: no se puede cumplir desde este programa. */
  OTRO_PROGRAMA
}
