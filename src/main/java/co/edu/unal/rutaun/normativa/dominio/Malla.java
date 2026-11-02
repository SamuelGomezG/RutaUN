package co.edu.unal.rutaun.normativa.dominio;

import co.edu.unal.rutaun.compartido.Creditos;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.util.List;
import java.util.stream.Stream;

/**
 * Árbol de nodos que representa el plan de estudios de un programa en una versión del consolidado
 * (glosario, «Malla»). Sus raíces son componentes; las agrupaciones van dentro de ellos.
 *
 * @param componentes los componentes de la malla
 */
public record Malla(List<NodoDeMalla> componentes) {

  /** Deja la lista inmutable y rechaza una agrupación suelta, que debe estar en un componente. */
  public Malla {
    componentes = ArgumentChecks.copiaSinNulos(componentes, "componentes");
    if (componentes.stream().anyMatch(Agrupacion.class::isInstance)) {
      throw new IllegalArgumentException("una agrupación debe estar dentro de un componente");
    }
  }

  /** Recorre todos los nodos de la malla, en profundidad, sin distinguir hojas de compuestos. */
  public Stream<NodoDeMalla> recorrer() {
    return componentes.stream().flatMap(NodoDeMalla::recorrer);
  }

  /** Todas las materias que figuran en la malla; una ubicada en varias agrupaciones se repite. */
  public List<MateriaEnMalla> materias() {
    return recorrer().flatMap(nodo -> nodo.materias().stream()).toList();
  }

  /** Suma los créditos que exige el plan de estudios a todos los componentes. */
  public Creditos creditosExigidos() {
    Creditos total = new Creditos(0);
    for (NodoDeMalla componente : componentes) {
      total = total.suma(componente.exigencia().exigidos());
    }
    return total;
  }
}
