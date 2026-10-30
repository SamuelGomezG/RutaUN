package co.edu.unal.rutaun.normativa;

import co.edu.unal.rutaun.compartido.EstadoRegla;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.util.Optional;

/**
 * Vista inmutable de una regla, tal como la muestra UC06.
 *
 * @param enunciado texto de la regla
 * @param fuente fuente de la regla; vacía si un error de datos la dejó «sin fuente registrada»
 *     (UC06 ext. 6e)
 * @param estado estado de la regla
 * @param temporal si la regla es temporal por su norma o por una vigencia propia
 */
public record ReglaVista(
    String enunciado, Optional<FuenteVista> fuente, EstadoRegla estado, boolean temporal) {

  /** Exige enunciado y estado; la fuente puede faltar, pero no ser null. */
  public ReglaVista {
    ArgumentChecks.obligatorioNoVacio(enunciado, "enunciado");
    ArgumentChecks.obligatorio(fuente, "fuente");
    ArgumentChecks.obligatorio(estado, "estado");
  }
}
