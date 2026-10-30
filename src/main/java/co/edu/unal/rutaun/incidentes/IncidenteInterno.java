package co.edu.unal.rutaun.incidentes;

import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.util.List;

/**
 * Incidente que no está atado a un programa: falta registrar el periodo en curso o un error interno
 * (UC01 ext. 11a, paso 2e).
 *
 * @param casoDeUso caso de uso en que ocurrió
 * @param paso paso o extensión en que ocurrió
 * @param causa qué ocurrió
 * @param elementos materias, agrupaciones, reglas o enlaces involucrados; puede estar vacía
 */
public record IncidenteInterno(String casoDeUso, String paso, String causa, List<String> elementos)
    implements IncidenteDetectado {

  /** Exige los datos y copia los elementos. */
  public IncidenteInterno {
    ArgumentChecks.obligatorioNoVacio(casoDeUso, "casoDeUso");
    ArgumentChecks.obligatorioNoVacio(paso, "paso");
    ArgumentChecks.obligatorioNoVacio(causa, "causa");
    elementos = List.copyOf(ArgumentChecks.obligatorio(elementos, "elementos"));
  }
}
