package co.edu.unal.rutaun.normativa;

import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.util.Optional;

/**
 * Fuente de una regla: la identificación de su norma, el artículo o numeral tal como lo rotula la
 * norma (glosario, «Fuente») y el enlace a Régimen Legal, cuando existe.
 *
 * @param norma identificación de la norma
 * @param articulo artículo o numeral, por ejemplo {@code art. 2 (transitorio)}
 * @param enlace enlace a Régimen Legal; vacío si la norma aún no está publicada allí
 */
public record FuenteVista(NormaVista norma, String articulo, Optional<String> enlace) {

  /** Exige norma y artículo; el enlace puede faltar, pero no ser null. */
  public FuenteVista {
    ArgumentChecks.obligatorio(norma, "norma");
    ArgumentChecks.obligatorioNoVacio(articulo, "articulo");
    ArgumentChecks.obligatorio(enlace, "enlace");
  }
}
