package co.edu.unal.rutaun.normativa.dominio;

import co.edu.unal.rutaun.normativa.TipoAlcance;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.text.Normalizer;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Órgano que expide una norma (diseño, «Tipos de apoyo»): nombre, nivel y, solo si es de facultad,
 * su facultad. Propone el alcance de una norma que no lo dice (UC07 ext. 4h; S11).
 *
 * <p>Dos órganos son el mismo si coinciden su nombre, sin contar tildes, mayúsculas ni espacios de
 * más, y su facultad (UC07 ext. 4c). Un sinónimo es otro órgano.
 *
 * @param nombre nombre del órgano, completo, tal como lo escribe la norma
 * @param nivel nivel del órgano
 * @param facultad facultad del órgano, solo cuando el nivel es {@link NivelDeOrgano#FACULTAD}
 */
public record Organo(String nombre, NivelDeOrgano nivel, Optional<Facultad> facultad) {

  /** Exige los tres datos y que la facultad vaya solo con el nivel de facultad. */
  public Organo {
    ArgumentChecks.obligatorioNoVacio(nombre, "organo");
    ArgumentChecks.obligatorio(nivel, "nivel");
    ArgumentChecks.obligatorio(facultad, "facultad");
    if ((nivel == NivelDeOrgano.FACULTAD) != facultad.isPresent()) {
      throw new IllegalArgumentException(
          nivel == NivelDeOrgano.FACULTAD
              ? "un órgano de facultad exige su facultad"
              : "solo un órgano de facultad lleva facultad");
    }
  }

  /**
   * Clave de comparación: el nombre sin tildes, en mayúsculas y con los espacios reducidos a uno,
   * seguido del código de su facultad, si la tiene.
   *
   * @return la clave con la que se comparan dos órganos
   */
  public String clave() {
    String base = normalizar(nombre);
    return facultad.map(f -> base + "|" + f.codigo()).orElse(base);
  }

  /**
   * Alcance que se propone cuando la norma no dice a qué programas aplica: todos, si el órgano es
   * nacional o de la Sede; los de su facultad, si es un Consejo de Facultad. Es una propuesta que
   * el administrador confirma o corrige (UC07 ext. 4h; S11).
   *
   * @param texto texto con el que se fija el alcance propuesto
   * @return el alcance propuesto
   */
  public Alcance alcancePropuesto(String texto) {
    return switch (nivel) {
      case NACIONAL, SEDE -> new Alcance(TipoAlcance.TODOS, Set.of(), Set.of(), Set.of(), texto);
      case FACULTAD ->
          new Alcance(
              TipoAlcance.FACULTADES, Set.of(facultad.orElseThrow()), Set.of(), Set.of(), texto);
    };
  }

  @Override
  public boolean equals(Object otro) {
    return otro instanceof Organo organo && clave().equals(organo.clave());
  }

  @Override
  public int hashCode() {
    return clave().hashCode();
  }

  private static String normalizar(String texto) {
    String sinTildes = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
    return sinTildes.strip().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
  }
}
