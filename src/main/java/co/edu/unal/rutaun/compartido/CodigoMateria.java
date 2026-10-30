package co.edu.unal.rutaun.compartido;

import co.edu.unal.rutaun.utilidades.ArgumentChecks;

/**
 * Código de una materia, que es su identidad en el catálogo único. El SRS no fija su formato, así
 * que solo se exige que no esté vacío.
 */
public record CodigoMateria(String valor) {

  /** Rechaza un código nulo o vacío. */
  public CodigoMateria {
    ArgumentChecks.obligatorioNoVacio(valor, "valor");
  }
}
