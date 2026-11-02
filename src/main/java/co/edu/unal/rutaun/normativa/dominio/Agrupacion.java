package co.edu.unal.rutaun.normativa.dominio;

import co.edu.unal.rutaun.compartido.AgrupacionId;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.util.List;

/**
 * Conjunto de materias de un componente con un eje temático y unos créditos exigidos (glosario,
 * «Agrupación»). Puede contener materias obligatorias y optativas y otras agrupaciones, sin límite
 * de niveles.
 *
 * @param id identificador de la agrupación
 * @param nombre nombre de la agrupación
 * @param exigencia créditos exigidos, incluidos los de sus subagrupaciones
 * @param materias materias que figuran directamente en ella
 * @param subagrupaciones agrupaciones que contiene
 */
public record Agrupacion(
    AgrupacionId id,
    String nombre,
    ExigenciaCreditos exigencia,
    List<MateriaEnMalla> materias,
    List<Agrupacion> subagrupaciones)
    implements NodoDeMalla {

  /** Exige todos los datos y deja las listas inmutables. */
  public Agrupacion {
    ArgumentChecks.obligatorio(id, "id");
    ArgumentChecks.obligatorioNoVacio(nombre, "nombre");
    ArgumentChecks.obligatorio(exigencia, "exigencia");
    materias = ArgumentChecks.copiaSinNulos(materias, "materias");
    subagrupaciones = ArgumentChecks.copiaSinNulos(subagrupaciones, "subagrupaciones");
  }

  @Override
  public List<NodoDeMalla> hijos() {
    return List.copyOf(subagrupaciones);
  }
}
