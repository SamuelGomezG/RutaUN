package co.edu.unal.rutaun.normativa.dominio;

import co.edu.unal.rutaun.compartido.Creditos;
import java.util.List;
import java.util.stream.Stream;

/**
 * Nodo de la malla (patrón Composite): un componente, el de libre elección o una agrupación. Quien
 * recorre la malla no distingue hojas de nodos compuestos: una hoja es un nodo sin {@link #hijos}.
 */
public sealed interface NodoDeMalla permits Componente, ComponenteDeLibreEleccion, Agrupacion {

  /** Nombre del nodo. */
  String nombre();

  /** Créditos que el plan de estudios exige al nodo. */
  ExigenciaCreditos exigencia();

  /** Materias que figuran directamente en el nodo, sin las de sus hijos. */
  List<MateriaEnMalla> materias();

  /** Nodos que contiene; vacía si es una hoja. */
  List<NodoDeMalla> hijos();

  /** Recorre el nodo y todo lo que contiene, en profundidad y empezando por él. */
  default Stream<NodoDeMalla> recorrer() {
    return Stream.concat(Stream.of(this), hijos().stream().flatMap(NodoDeMalla::recorrer));
  }

  /** Todas las materias que figuran en el nodo y en lo que contiene. */
  default List<MateriaEnMalla> todasLasMaterias() {
    return recorrer().flatMap(nodo -> nodo.materias().stream()).toList();
  }

  /**
   * Suma los créditos de todas las materias del nodo y de lo que contiene, incluidas las optativas,
   * que son alternativas entre sí: es la oferta de créditos, no lo que cubre el nodo.
   */
  default Creditos creditosDeMaterias() {
    Creditos total = new Creditos(0);
    for (MateriaEnMalla materia : materias()) {
      total = total.suma(materia.creditos());
    }
    for (NodoDeMalla hijo : hijos()) {
      total = total.suma(hijo.creditosDeMaterias());
    }
    return total;
  }
}
