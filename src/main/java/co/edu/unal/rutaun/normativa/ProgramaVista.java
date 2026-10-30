package co.edu.unal.rutaun.normativa;

import co.edu.unal.rutaun.compartido.ProgramaId;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;

/**
 * Vista inmutable de un programa.
 *
 * @param id identificador del programa
 * @param nombre nombre del programa
 * @param codigoSia código SIA del programa
 * @param facultad facultad a la que pertenece
 */
public record ProgramaVista(ProgramaId id, String nombre, String codigoSia, String facultad) {

  /** Exige todos los datos. */
  public ProgramaVista {
    ArgumentChecks.obligatorio(id, "id");
    ArgumentChecks.obligatorioNoVacio(nombre, "nombre");
    ArgumentChecks.obligatorioNoVacio(codigoSia, "codigoSia");
    ArgumentChecks.obligatorioNoVacio(facultad, "facultad");
  }
}
