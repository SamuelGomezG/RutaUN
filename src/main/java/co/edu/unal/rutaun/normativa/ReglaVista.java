package co.edu.unal.rutaun.normativa;

import co.edu.unal.rutaun.compartido.EstadoRegla;
import co.edu.unal.rutaun.compartido.ReglaId;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.util.Optional;

/**
 * Vista inmutable de una regla, tal como la muestra UC06.
 *
 * @param id identificador de la regla, con el que se abre completa (UC06 paso 8)
 * @param enunciado texto de la regla
 * @param fuente fuente de la regla; vacía si un error de datos la dejó «sin fuente registrada»
 *     (UC06 ext. 6e)
 * @param estado estado de la regla
 * @param temporal si la regla es temporal por su norma o por una vigencia propia
 * @param clasificacion si el plan la aplica o solo la informa (UC06 paso 6, ext. 6c)
 */
public record ReglaVista(
    ReglaId id,
    String enunciado,
    Optional<FuenteVista> fuente,
    EstadoRegla estado,
    boolean temporal,
    ClasificacionRegla clasificacion) {

  /**
   * Exige identificador, enunciado, estado y clasificación; la fuente puede faltar, pero no ser
   * null.
   */
  public ReglaVista {
    ArgumentChecks.obligatorio(id, "id");
    ArgumentChecks.obligatorioNoVacio(enunciado, "enunciado");
    ArgumentChecks.obligatorio(fuente, "fuente");
    ArgumentChecks.obligatorio(estado, "estado");
    ArgumentChecks.obligatorio(clasificacion, "clasificacion");
  }
}
