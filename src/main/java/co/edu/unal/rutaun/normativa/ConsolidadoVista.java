package co.edu.unal.rutaun.normativa;

import co.edu.unal.rutaun.compartido.ProgramaId;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Vista inmutable del consolidado vigente de un programa: sus reglas agrupadas por categoría, con
 * la versión y la fecha de actualización (UC06 paso 4, GE 4).
 *
 * @param programa programa al que pertenece el consolidado
 * @param version número de la versión vigente
 * @param fecha fecha de la versión
 * @param reglasPorCategoria reglas vigentes y programadas, por nombre de categoría
 */
public record ConsolidadoVista(
    ProgramaId programa,
    int version,
    LocalDate fecha,
    Map<String, List<ReglaVista>> reglasPorCategoria) {

  /** Exige todos los datos y copia el mapa y sus listas para que nadie pueda modificarlos. */
  public ConsolidadoVista {
    ArgumentChecks.obligatorio(programa, "programa");
    ArgumentChecks.obligatorioNoNegativo(version, "version");
    ArgumentChecks.obligatorio(fecha, "fecha");
    reglasPorCategoria =
        ArgumentChecks.obligatorio(reglasPorCategoria, "reglasPorCategoria").entrySet().stream()
            .collect(
                Collectors.toUnmodifiableMap(Map.Entry::getKey, e -> List.copyOf(e.getValue())));
  }
}
