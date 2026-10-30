package co.edu.unal.rutaun.normativa;

import co.edu.unal.rutaun.compartido.ProgramaId;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Vista inmutable del consolidado vigente de un programa: sus reglas agrupadas por categoría, con
 * la versión y la fecha de actualización (UC06 paso 4, GE 4).
 *
 * @param programa programa al que pertenece el consolidado
 * @param version número de la versión vigente
 * @param fecha fecha de la versión
 * @param reglasPorCategoria reglas vigentes y programadas, por nombre de categoría (con el
 *     vocabulario del glosario, «Regla»: «Prerrequisitos y habilitación», «Otra»…) y en el orden
 *     del consolidado: el mismo dato produce siempre la misma vista. Vacío si la versión no tiene
 *     reglas cargadas (UC06 ext. 4a); una categoría presente tiene al menos una regla
 */
public record ConsolidadoVista(
    ProgramaId programa,
    int version,
    LocalDate fecha,
    Map<String, List<ReglaVista>> reglasPorCategoria) {

  /**
   * Exige todos los datos y copia el mapa y sus listas, conservando el orden de las categorías,
   * para que nadie pueda modificarlos. Rechaza una categoría sin reglas, para que «sin reglas»
   * (UC06 ext. 4a) se lea siempre como un mapa vacío.
   */
  public ConsolidadoVista {
    ArgumentChecks.obligatorio(programa, "programa");
    ArgumentChecks.obligatorioNoNegativo(version, "version");
    ArgumentChecks.obligatorio(fecha, "fecha");
    Map<String, List<ReglaVista>> copia = new LinkedHashMap<>();
    ArgumentChecks.obligatorio(reglasPorCategoria, "reglasPorCategoria")
        .forEach(
            (categoria, reglas) ->
                copia.put(
                    ArgumentChecks.obligatorioNoVacio(categoria, "categoria"),
                    reglasDeUnaCategoria(reglas)));
    reglasPorCategoria = Collections.unmodifiableMap(copia);
  }

  /** Si la versión no tiene reglas cargadas (UC06 ext. 4a). */
  public boolean sinReglas() {
    return reglasPorCategoria.isEmpty();
  }

  private static List<ReglaVista> reglasDeUnaCategoria(List<ReglaVista> reglas) {
    if (ArgumentChecks.obligatorio(reglas, "reglas").isEmpty()) {
      throw new IllegalArgumentException("reglas no puede ser vacío");
    }
    return List.copyOf(reglas);
  }
}
