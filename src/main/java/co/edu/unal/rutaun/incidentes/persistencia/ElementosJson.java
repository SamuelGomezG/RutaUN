package co.edu.unal.rutaun.incidentes.persistencia;

import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.util.List;

/**
 * Forma en que se guardan los elementos de un incidente en la columna {@code incidente.elementos},
 * como JSON.
 *
 * @param esquema versión del esquema
 * @param elementos elementos involucrados, en orden
 */
public record ElementosJson(int esquema, List<String> elementos) {

  /** Versión del esquema de este JSON. */
  public static final int ESQUEMA = 1;

  /** Exige el esquema conocido y deja la lista inmutable. */
  public ElementosJson {
    if (esquema != ESQUEMA) {
      throw new IllegalArgumentException("esquema desconocido: " + esquema);
    }
    elementos = ArgumentChecks.copiaSinNulos(elementos, "elementos");
  }
}
