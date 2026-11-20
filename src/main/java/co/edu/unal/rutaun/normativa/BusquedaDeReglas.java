package co.edu.unal.rutaun.normativa;

import co.edu.unal.rutaun.compartido.EstadoRegla;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.util.Optional;

/**
 * Lo que el visitante pide al buscar y filtrar las reglas de un programa (UC06 paso 6). Cada dato
 * es opcional y se combinan con «y»: una regla sale si cumple todos los que se dieron.
 *
 * <p>Sin {@code estado}, la búsqueda recorre las reglas que rigen, las programadas y las que están
 * en revisión, como el listado normal. Las que ya no rigen solo salen si se pide ese estado, y
 * entonces salen solas, sin mezclarse con las vigentes (ext. 5b).
 *
 * @param texto palabras que deben aparecer en el enunciado, en el acuerdo o en el artículo, sin
 *     distinguir mayúsculas ni tildes; vacío si no se busca por texto
 * @param categoria nombre de la categoría (glosario, «Regla»); vacío si no se filtra por categoría
 * @param estado estado que deben tener las reglas; vacío para las que rigen, las programadas y las
 *     que están en revisión
 */
public record BusquedaDeReglas(
    Optional<String> texto, Optional<String> categoria, Optional<EstadoRegla> estado) {

  /** La búsqueda sin texto ni filtros: el listado normal del consolidado. */
  public static final BusquedaDeReglas TODAS =
      new BusquedaDeReglas(Optional.empty(), Optional.empty(), Optional.empty());

  /** Exige los datos y descarta el texto o la categoría que solo traen espacios. */
  public BusquedaDeReglas {
    texto = soloSiTrae(ArgumentChecks.obligatorio(texto, "texto"));
    categoria = soloSiTrae(ArgumentChecks.obligatorio(categoria, "categoria"));
    ArgumentChecks.obligatorio(estado, "estado");
  }

  /**
   * Arma la búsqueda con lo que llega de la página, donde un dato que falta es {@code null}.
   *
   * @param texto texto buscado, o {@code null}
   * @param categoria categoría elegida, o {@code null}
   * @param estado estado elegido, o {@code null}
   * @return la búsqueda con esos datos
   */
  public static BusquedaDeReglas de(String texto, String categoria, EstadoRegla estado) {
    return new BusquedaDeReglas(
        Optional.ofNullable(texto), Optional.ofNullable(categoria), Optional.ofNullable(estado));
  }

  /** Si no trae texto ni filtros: es el listado normal. */
  public boolean esVacia() {
    return texto.isEmpty() && categoria.isEmpty() && estado.isEmpty();
  }

  private static Optional<String> soloSiTrae(Optional<String> valor) {
    return valor.map(String::strip).filter(contenido -> !contenido.isEmpty());
  }
}
