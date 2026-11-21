package co.edu.unal.rutaun.normativa;

import co.edu.unal.rutaun.compartido.CodigoMateria;
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
   * Las reglas del consolidado vigente que cumplen la búsqueda (UC06 paso 6): por texto, por
   * categoría y por estado. Con la búsqueda vacía, es el {@link #consolidadoVigente}. Con el estado
   * «ya no rige» devuelve esas reglas, y solo esas, con su vigencia (ext. 5b). Si ninguna cumple la
   * búsqueda (ext. 5a), devuelve el consolidado con {@link ConsolidadoVista#reglasPorCategoria()}
   * vacío. Vacío solo si el programa no está cargado o no tiene una versión vigente del
   * consolidado.
   */
  Optional<ConsolidadoVista> buscarReglas(ProgramaId id, BusquedaDeReglas busqueda);

  /** Los nombres de las categorías de reglas, en el orden del glosario, para filtrar por ellas. */
  List<String> categoriasDeReglas();

  /**
   * La regla completa, tal como la muestra UC06 al abrirla (paso 8), con la cadena de normas que la
   * expidieron, la modificaron, la derogaron o la exceptuaron: es la misma cadena se abra la regla
   * original o la que la modifica. Abre también las reglas que ya no rigen, con ese estado. Vacío
   * si el programa no tiene una versión vigente o si la regla no está en ella.
   */
  Optional<ReglaCompletaVista> regla(ProgramaId programa, ReglaId regla);

  /**
   * Lo que la materia exige para cursarla en el consolidado vigente del programa (UC05 pasos 2 a
   * 4): sus prerrequisitos y correquisitos, cada uno con lo que exige, su fuente, su fecha de
   * aplicación y su vigencia. Incluye las reglas vigentes, programadas y en revisión, y deja fuera
   * las que ya no rigen. No muestra las materias externas (ext. 4k). Una materia que no exige nada
   * (ext. 3a) devuelve la vista con {@link RestriccionesDeMateriaVista#restricciones()} vacía.
   * Vacío si el programa no está cargado, no tiene una versión vigente del consolidado o la materia
   * no figura en ella.
   */
  Optional<RestriccionesDeMateriaVista> restriccionesDe(ProgramaId programa, CodigoMateria materia);
}
