package co.edu.unal.rutaun.normativa.aplicacion;

import co.edu.unal.rutaun.normativa.TipoAlcance;
import co.edu.unal.rutaun.normativa.dominio.Facultad;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.util.Set;

/**
 * Alcance de una norma tal como viene en el JSON: las facultades y los programas se identifican por
 * nombre o código SIA, no por los identificadores internos que todavía no existen al leer.
 *
 * @param tipo tipo del alcance
 * @param facultades nombres de las facultades comprendidas
 * @param programas códigos SIA de los programas comprendidos
 * @param exclusiones códigos SIA de los programas excluidos
 * @param texto texto con el que la norma fija el alcance
 */
public record AlcanceLeida(
    TipoAlcance tipo,
    Set<Facultad> facultades,
    Set<String> programas,
    Set<String> exclusiones,
    String texto) {

  /** Valida los datos y deja las colecciones inmutables. */
  public AlcanceLeida {
    ArgumentChecks.obligatorio(tipo, "tipo");
    facultades = ArgumentChecks.copiaSinNulosDeConjunto(facultades, "facultades");
    programas = ArgumentChecks.copiaSinNulosDeConjunto(programas, "programas");
    exclusiones = ArgumentChecks.copiaSinNulosDeConjunto(exclusiones, "exclusiones");
    texto = ArgumentChecks.obligatorioNoVacio(texto, "texto");
    validarSegunTipo(tipo, facultades, programas, exclusiones);
  }

  private static void validarSegunTipo(
      TipoAlcance tipo, Set<Facultad> facultades, Set<String> programas, Set<String> exclusiones) {
    switch (tipo) {
      case TODOS -> {
        if (!facultades.isEmpty() || !programas.isEmpty()) {
          throw new IllegalArgumentException(
              "un alcance de todos no lleva facultades ni programas");
        }
      }
      case FACULTADES -> {
        if (facultades.isEmpty()) {
          throw new IllegalArgumentException("un alcance por facultades debe indicar al menos una");
        }
        if (!programas.isEmpty()) {
          throw new IllegalArgumentException("un alcance por facultades no lleva programas");
        }
      }
      case PROGRAMAS -> {
        if (programas.isEmpty()) {
          throw new IllegalArgumentException("un alcance por programas debe indicar al menos uno");
        }
        if (!facultades.isEmpty()) {
          throw new IllegalArgumentException("un alcance por programas no lleva facultades");
        }
        if (!programas.containsAll(exclusiones)) {
          throw new IllegalArgumentException("las exclusiones deben ser programas del alcance");
        }
      }
      default -> throw new IllegalStateException("tipo de alcance no soportado: " + tipo);
    }
  }
}
