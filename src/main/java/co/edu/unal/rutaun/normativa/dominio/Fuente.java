package co.edu.unal.rutaun.normativa.dominio;

import co.edu.unal.rutaun.compartido.NormaId;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;

/**
 * Fuente de una regla: la norma que la contiene y el artículo o numeral tal como lo rotula la norma
 * (glosario, «Fuente»). Toda regla tiene fuente; por eso sus dos datos son obligatorios.
 *
 * @param norma identificador de la norma
 * @param articulo artículo o numeral, por ejemplo {@code art. 2 (transitorio)}
 */
public record Fuente(NormaId norma, String articulo) {

  /** Exige la norma y un artículo o numeral no vacío. */
  public Fuente {
    ArgumentChecks.obligatorio(norma, "norma");
    ArgumentChecks.obligatorioNoVacio(articulo, "articulo");
  }
}
