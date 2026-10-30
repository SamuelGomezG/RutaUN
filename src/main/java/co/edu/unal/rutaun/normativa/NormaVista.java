package co.edu.unal.rutaun.normativa;

import co.edu.unal.rutaun.utilidades.ArgumentChecks;

/**
 * Identificación de una norma, tal como la muestra UC06: tipo, número, año y órgano que la expide.
 * El número es texto porque las normas lo rotulan así (Acuerdo 003-A de 2022).
 *
 * @param tipo tipo de la norma
 * @param numero número tal como lo rotula la norma
 * @param anio año de expedición
 * @param organo órgano que la expide
 */
public record NormaVista(TipoNorma tipo, String numero, int anio, String organo) {

  /** Exige todos los datos. */
  public NormaVista {
    ArgumentChecks.obligatorio(tipo, "tipo");
    ArgumentChecks.obligatorioNoVacio(numero, "numero");
    ArgumentChecks.obligatorioPositivo(anio, "anio");
    ArgumentChecks.obligatorioNoVacio(organo, "organo");
  }
}
