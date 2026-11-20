package co.edu.unal.rutaun.normativa.aplicacion;

import co.edu.unal.rutaun.normativa.ProgramaVista;
import co.edu.unal.rutaun.normativa.ReglaCompletaVista;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;

/**
 * Una regla abierta completa (UC06 paso 8), con el programa al que pertenece su consolidado.
 *
 * @param programa el programa consultado
 * @param regla la regla completa
 */
public record ReglaDelPrograma(ProgramaVista programa, ReglaCompletaVista regla) {

  /** Exige los dos datos. */
  public ReglaDelPrograma {
    ArgumentChecks.obligatorio(programa, "programa");
    ArgumentChecks.obligatorio(regla, "regla");
  }
}
