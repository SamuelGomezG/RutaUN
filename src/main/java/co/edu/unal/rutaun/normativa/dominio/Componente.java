package co.edu.unal.rutaun.normativa.dominio;

import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.util.List;

/**
 * Parte del plan de estudios con unos créditos exigidos, como fundamentación o disciplinar
 * (glosario, «Componente»). Tiene la misma estructura que una agrupación: puede contener materias y
 * agrupaciones.
 *
 * @param nombre nombre del componente
 * @param tipo tipo del componente
 * @param exigencia créditos exigidos, incluidos los de todo lo que contiene
 * @param materias materias que figuran directamente en él
 * @param agrupaciones agrupaciones que contiene
 */
public record Componente(
    String nombre,
    TipoComponente tipo,
    ExigenciaCreditos exigencia,
    List<MateriaEnMalla> materias,
    List<Agrupacion> agrupaciones)
    implements NodoDeMalla {

  /** Exige todos los datos y deja las listas inmutables. */
  public Componente {
    ArgumentChecks.obligatorioNoVacio(nombre, "nombre");
    ArgumentChecks.obligatorio(tipo, "tipo");
    ArgumentChecks.obligatorio(exigencia, "exigencia");
    materias = ArgumentChecks.copiaSinNulos(materias, "materias");
    agrupaciones = ArgumentChecks.copiaSinNulos(agrupaciones, "agrupaciones");
  }

  @Override
  public List<NodoDeMalla> hijos() {
    return List.copyOf(agrupaciones);
  }
}
