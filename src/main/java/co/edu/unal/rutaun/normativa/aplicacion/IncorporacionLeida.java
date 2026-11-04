package co.edu.unal.rutaun.normativa.aplicacion;

import co.edu.unal.rutaun.normativa.dominio.Regla;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.util.List;

/**
 * Borrador de una incorporación leída desde JSON: el programa por código SIA, la norma con su
 * alcance sin resolver y las reglas. El programa se resuelve contra los ya cargados por SOP-03; el
 * lector no crea programas ni identificadores de programa.
 *
 * @param programa programa al que se incorpora la norma
 * @param norma norma leída, con identificador de norma provisional
 * @param reglas reglas de la norma, en el orden del archivo
 */
public record IncorporacionLeida(ProgramaJson programa, NormaLeida norma, List<Regla> reglas) {

  /** Exige los tres datos y deja la lista de reglas inmutable. */
  public IncorporacionLeida {
    ArgumentChecks.obligatorio(programa, "programa");
    ArgumentChecks.obligatorio(norma, "norma");
    reglas = ArgumentChecks.copiaSinNulos(reglas, "reglas");
  }
}
