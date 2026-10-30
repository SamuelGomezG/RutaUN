package co.edu.unal.rutaun.normativa;

import co.edu.unal.rutaun.compartido.ProgramaId;
import java.util.List;
import java.util.Optional;

/**
 * Fachada pública de consulta de Normativa. No exige sesión (UC05, UC06) y devuelve vistas
 * inmutables, nunca entidades ni objetos del dominio.
 */
public interface ConsultaNormativaFacade {

  /** Los programas cargados (UC06 paso 2). */
  List<ProgramaVista> programas();

  /** El programa con ese identificador, si está cargado. */
  Optional<ProgramaVista> programa(ProgramaId id);

  /**
   * El consolidado vigente del programa, con su versión y su fecha. Si la versión vigente no tiene
   * reglas cargadas (UC06 ext. 4a), devuelve el consolidado con {@link
   * ConsolidadoVista#reglasPorCategoria()} vacío, para que el incidente cite esa versión. Vacío
   * solo si el programa no está cargado o no tiene una versión vigente del consolidado.
   */
  Optional<ConsolidadoVista> consolidadoVigente(ProgramaId id);
}
