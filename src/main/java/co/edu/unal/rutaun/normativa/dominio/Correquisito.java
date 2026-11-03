package co.edu.unal.rutaun.normativa.dominio;

import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;

/**
 * Materias que deben cursarse en el mismo semestre que la materia o antes (glosario,
 * «Correquisito»). Comparte con el prerrequisito la forma de la expresión y la regla de las
 * materias externas, pero no exige haber aprobado antes.
 *
 * @param materia materia que exige el correquisito
 * @param expresion materias que lo satisfacen
 */
public record Correquisito(CodigoMateria materia, ExpresionDeMaterias expresion)
    implements Restriccion {

  /** Exige la materia y la expresión. */
  public Correquisito {
    ArgumentChecks.obligatorio(materia, "materia");
    ArgumentChecks.obligatorio(expresion, "expresion");
  }
}
