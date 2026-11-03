package co.edu.unal.rutaun.normativa.aplicacion;

import co.edu.unal.rutaun.compartido.ProgramaId;
import co.edu.unal.rutaun.normativa.dominio.Facultad;
import co.edu.unal.rutaun.normativa.dominio.Programa;
import co.edu.unal.rutaun.normativa.persistencia.ProgramaEntidad;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;

/**
 * Convierte entre la raíz {@link Programa} y su fila {@link ProgramaEntidad}. No guarda estado.
 *
 * <p>TT-04-T3 (#91). Reconstruye con {@link Programa#reconstituir}, así que no genera un
 * identificador nuevo, no emite eventos y pasa por las mismas validaciones que un programa creado.
 * Si la base trae un dato inválido, sube la {@link IllegalArgumentException} del dominio.
 */
public final class ProgramaMapper {

  private ProgramaMapper() {}

  /** Reconstruye el programa que guarda esta fila. */
  public static Programa aDominio(ProgramaEntidad entidad) {
    ArgumentChecks.obligatorio(entidad, "entidad");
    return Programa.reconstituir(
        new ProgramaId(entidad.getId()),
        entidad.getNombre(),
        entidad.getCodigoSia(),
        new Facultad(entidad.getFacultad()));
  }

  /** Convierte el programa en la fila que se guarda en la base. */
  public static ProgramaEntidad aEntidad(Programa programa) {
    ArgumentChecks.obligatorio(programa, "programa");
    return new ProgramaEntidad(
        programa.id().valor(),
        programa.nombre(),
        programa.codigoSia(),
        programa.facultad().nombre());
  }
}
