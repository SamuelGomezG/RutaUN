package co.edu.unal.rutaun.incidentes;

import co.edu.unal.rutaun.compartido.ProgramaId;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.util.List;

/**
 * Incidente de un programa y de la versión de su consolidado: una regla sin fuente o en revisión,
 * una malla que no es utilizable, un prerrequisito circular, un consolidado sin reglas (UC06 ext.
 * 4a), una materia que ya no rige sin cómo contarla.
 *
 * @param casoDeUso caso de uso en que ocurrió
 * @param paso paso o extensión en que ocurrió
 * @param programa programa involucrado
 * @param versionDelConsolidado versión del consolidado, desde la 1; toda forma de incidente con
 *     programa cita también su versión
 * @param causa qué ocurrió
 * @param elementos materias, agrupaciones, reglas o enlaces involucrados; puede estar vacía
 */
public record IncidenteDePrograma(
    String casoDeUso,
    String paso,
    ProgramaId programa,
    int versionDelConsolidado,
    String causa,
    List<String> elementos)
    implements IncidenteDetectado {

  /** Exige todos los datos y copia los elementos. */
  public IncidenteDePrograma {
    ArgumentChecks.obligatorioNoVacio(casoDeUso, "casoDeUso");
    ArgumentChecks.obligatorioNoVacio(paso, "paso");
    ArgumentChecks.obligatorio(programa, "programa");
    ArgumentChecks.obligatorioPositivo(versionDelConsolidado, "versionDelConsolidado");
    ArgumentChecks.obligatorioNoVacio(causa, "causa");
    elementos = List.copyOf(ArgumentChecks.obligatorio(elementos, "elementos"));
  }
}
