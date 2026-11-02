package co.edu.unal.rutaun.normativa.dominio;

import co.edu.unal.rutaun.compartido.ProgramaId;
import co.edu.unal.rutaun.normativa.TipoAlcance;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.util.Set;

/**
 * Programas a los que aplica una norma según su propio texto (glosario, «Alcance»). Guarda el tipo
 * del alcance, las facultades o los programas que comprende, los programas excluidos y el texto con
 * que la norma lo fija.
 *
 * @param tipo tipo del alcance
 * @param facultades facultades comprendidas cuando el tipo es {@link TipoAlcance#FACULTADES}
 * @param programas programas comprendidos cuando el tipo es {@link TipoAlcance#PROGRAMAS}
 * @param exclusiones programas excluidos del alcance
 * @param texto texto con el que la norma fija el alcance
 */
public record Alcance(
    TipoAlcance tipo,
    Set<Facultad> facultades,
    Set<ProgramaId> programas,
    Set<ProgramaId> exclusiones,
    String texto) {

  /** Exige el tipo y el texto, copia las colecciones y valida la coherencia con el tipo. */
  public Alcance {
    ArgumentChecks.obligatorio(tipo, "tipo");
    facultades = ArgumentChecks.copiaSinNulosDeConjunto(facultades, "facultades");
    programas = ArgumentChecks.copiaSinNulosDeConjunto(programas, "programas");
    exclusiones = ArgumentChecks.copiaSinNulosDeConjunto(exclusiones, "exclusiones");
    texto = ArgumentChecks.obligatorioNoVacio(texto, "texto");
    validarSegunTipo(tipo, facultades, programas, exclusiones);
  }

  /**
   * Si el alcance comprende al programa: aplica el tipo y descarta los programas excluidos.
   *
   * @param programa programa que se quiere comprobar
   * @return {@code true} si la norma aplica a ese programa
   */
  public boolean comprende(Programa programa) {
    ArgumentChecks.obligatorio(programa, "programa");
    if (exclusiones.contains(programa.id())) {
      return false;
    }
    return switch (tipo) {
      case TODOS -> true;
      case FACULTADES -> facultades.contains(programa.facultad());
      case PROGRAMAS -> programas.contains(programa.id());
    };
  }

  private static void validarSegunTipo(
      TipoAlcance tipo,
      Set<Facultad> facultades,
      Set<ProgramaId> programas,
      Set<ProgramaId> exclusiones) {
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
