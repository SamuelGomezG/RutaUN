package co.edu.unal.rutaun.normativa.dominio;

import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;

/**
 * Materia, o conjunto de materias combinadas con «y» y «o», que debe estar aprobada o en curso
 * antes de cursar otra (glosario, «Prerrequisito»).
 *
 * @param materia materia que exige el prerrequisito
 * @param expresion materias que lo satisfacen
 */
public record Prerrequisito(CodigoMateria materia, ExpresionDeMaterias expresion)
    implements Restriccion {

  /** Exige la materia y la expresión. */
  public Prerrequisito {
    ArgumentChecks.obligatorio(materia, "materia");
    ArgumentChecks.obligatorio(expresion, "expresion");
  }
}
