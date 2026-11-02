package co.edu.unal.rutaun.normativa.dominio;

import co.edu.unal.rutaun.utilidades.ArgumentChecks;

/**
 * Facultad de la Universidad (por ejemplo, «Ingeniería»). La usa {@link Programa} y, más adelante,
 * el órgano y el alcance de una norma.
 *
 * @param nombre nombre de la facultad
 */
public record Facultad(String nombre) {

  /** Rechaza un nombre nulo o vacío. */
  public Facultad {
    ArgumentChecks.obligatorioNoVacio(nombre, "nombre");
  }
}
