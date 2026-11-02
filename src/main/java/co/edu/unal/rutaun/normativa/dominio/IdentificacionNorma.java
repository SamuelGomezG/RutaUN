package co.edu.unal.rutaun.normativa.dominio;

import co.edu.unal.rutaun.normativa.TipoNorma;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;

/**
 * Identificación de una norma: tipo, número, año y órgano que la expide (glosario, «Acuerdo (o
 * norma)»). Una norma se registra una sola vez con esta identificación.
 *
 * @param tipo tipo de la norma
 * @param numero número tal como lo rotula la norma
 * @param anio año de expedición
 * @param organo órgano que la expide
 */
public record IdentificacionNorma(TipoNorma tipo, String numero, int anio, String organo) {

  /** Exige todos los datos y un año positivo. */
  public IdentificacionNorma {
    ArgumentChecks.obligatorio(tipo, "tipo");
    ArgumentChecks.obligatorioNoVacio(numero, "numero");
    ArgumentChecks.obligatorioPositivo(anio, "anio");
    ArgumentChecks.obligatorioNoVacio(organo, "organo");
  }
}
