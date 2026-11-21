package co.edu.unal.rutaun.normativa.dominio;

import co.edu.unal.rutaun.compartido.AgrupacionId;
import co.edu.unal.rutaun.compartido.Creditos;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Árbol de nodos que representa el plan de estudios de un programa en una versión del consolidado
 * (glosario, «Malla»). Sus raíces son los tres componentes del Acuerdo 033 de 2007, art. 9:
 * fundamentación, disciplinar o profesional, y libre elección. Las agrupaciones van dentro de ellos
 * y pertenecen a un solo componente o a una sola agrupación (glosario, «Agrupación»).
 *
 * <p>La validación vive en el constructor compacto porque el constructor canónico de un record
 * público no puede tener menos acceso que el record: así la fábrica, la reconstrucción y cualquier
 * otro camino fallan ante una malla con forma inválida. Quien deba explicar el motivo al
 * administrador (UC07 ext. 6d) comprueba la forma antes de construir la malla, no atrapando esta
 * excepción.
 *
 * @param componentes los componentes de la malla
 */
public record Malla(List<NodoDeMalla> componentes) {

  /**
   * Deja la lista inmutable y exige la forma de la malla: exactamente un componente de
   * fundamentación, uno disciplinar y uno de libre elección, sin agrupaciones sueltas y sin
   * agrupaciones repetidas ni compartidas entre padres.
   */
  public Malla {
    componentes = ArgumentChecks.copiaSinNulos(componentes, "componentes");
    EnumMap<Raiz, Integer> conteos = new EnumMap<>(Raiz.class);
    for (NodoDeMalla raiz : componentes) {
      conteos.merge(raizDe(raiz), 1, Integer::sum);
    }
    int fundamentacion = conteos.getOrDefault(Raiz.FUNDAMENTACION, 0);
    int disciplinar = conteos.getOrDefault(Raiz.DISCIPLINAR, 0);
    int libreEleccion = conteos.getOrDefault(Raiz.LIBRE_ELECCION, 0);
    if (fundamentacion != 1 || disciplinar != 1 || libreEleccion != 1) {
      throw new IllegalArgumentException(
          "la malla debe tener exactamente un componente de fundamentación, uno disciplinar y uno"
              + " de libre elección (Acuerdo 033 de 2007, art. 9); tiene "
              + fundamentacion
              + " de fundamentación, "
              + disciplinar
              + " de disciplinar y "
              + libreEleccion
              + " de libre elección");
    }
    Map<AgrupacionId, Agrupacion> vistas = new HashMap<>();
    componentes.stream()
        .flatMap(NodoDeMalla::recorrer)
        .flatMap(Malla::agrupacionesDe)
        .forEach(agrupacion -> registrar(vistas, agrupacion));
  }

  /**
   * Crea la malla con sus tres componentes. Los argumentos documentan la forma esperada; el tipo de
   * cada uno lo sigue comprobando el constructor.
   */
  public static Malla de(
      Componente fundamentacion, Componente disciplinar, ComponenteDeLibreEleccion libreEleccion) {
    return new Malla(
        List.of(
            ArgumentChecks.obligatorio(fundamentacion, "fundamentacion"),
            ArgumentChecks.obligatorio(disciplinar, "disciplinar"),
            ArgumentChecks.obligatorio(libreEleccion, "libreEleccion")));
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

  /** Clasifica una raíz por el papel que le da la forma de la malla. */
  private static Raiz raizDe(NodoDeMalla nodo) {
    return switch (nodo) {
      case Agrupacion ignored ->
          throw new IllegalArgumentException("una agrupación debe estar dentro de un componente");
      case Componente componente ->
          switch (componente.tipo()) {
            case FUNDAMENTACION -> Raiz.FUNDAMENTACION;
            case DISCIPLINAR -> Raiz.DISCIPLINAR;
          };
      case ComponenteDeLibreEleccion ignored -> Raiz.LIBRE_ELECCION;
    };
  }

  /** Las agrupaciones que hay en un nodo y en lo que contiene. */
  private static Stream<Agrupacion> agrupacionesDe(NodoDeMalla nodo) {
    return switch (nodo) {
      case Agrupacion agrupacion -> Stream.of(agrupacion);
      case Componente ignored -> Stream.empty();
      case ComponenteDeLibreEleccion ignored -> Stream.empty();
    };
  }

  /** Registra una agrupación y rechaza un id que ya apareció en otro lugar del árbol. */
  private static void registrar(Map<AgrupacionId, Agrupacion> vistas, Agrupacion agrupacion) {
    Agrupacion previa = vistas.putIfAbsent(agrupacion.id(), agrupacion);
    if (previa == null) {
      return;
    }
    if (previa.equals(agrupacion)) {
      throw new IllegalArgumentException(
          "la agrupación «"
              + agrupacion.nombre()
              + "» aparece dos veces en la malla: pertenece a un solo componente o a una sola"
              + " agrupación");
    }
    throw new IllegalArgumentException(
        "dos agrupaciones distintas usan el id "
            + agrupacion.id().valor()
            + ": «"
            + previa.nombre()
            + "» y «"
            + agrupacion.nombre()
            + "»");
  }

  /** Los tres papeles que puede tener una raíz de la malla. */
  private enum Raiz {
    FUNDAMENTACION,
    DISCIPLINAR,
    LIBRE_ELECCION
  }
}
