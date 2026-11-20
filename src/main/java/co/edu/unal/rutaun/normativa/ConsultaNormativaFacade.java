package co.edu.unal.rutaun.normativa;

import co.edu.unal.rutaun.compartido.ProgramaId;
import co.edu.unal.rutaun.compartido.ReglaId;
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

  /**
   * La regla completa, tal como la muestra UC06 al abrirla (paso 8), con la cadena de normas que la
   * expidieron, la modificaron, la derogaron o la exceptuaron: es la misma cadena se abra la regla
   * original o la que la modifica. Abre también las reglas que ya no rigen, con ese estado. Vacío
   * si el programa no tiene una versión vigente o si la regla no está en ella.
   */
  Optional<ReglaCompletaVista> regla(ProgramaId programa, ReglaId regla);
}
