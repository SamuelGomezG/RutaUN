package co.edu.unal.rutaun.incidentes;

import co.edu.unal.rutaun.compartido.EventoDeDominio;
import java.util.List;

/**
 * Aviso de que un contexto detectó algo que el administrador debe conocer (SRS, §4.6). Lo define
 * Incidentes, que es quien necesita que le avisen, y lo publican los demás contextos. No lleva
 * fecha: la pone Incidentes con su reloj. Sellado con una variante por forma: con programa (y la
 * versión de su consolidado) o sin programa (por ejemplo, falta registrar el periodo en curso o un
 * error interno).
 */
public sealed interface IncidenteDetectado extends EventoDeDominio
    permits IncidenteDePrograma, IncidenteInterno {

  /** Caso de uso en que ocurrió, por ejemplo {@code UC06}. */
  String casoDeUso();

  /** Paso o extensión en que ocurrió, por ejemplo {@code ext. 4a}. */
  String paso();

  /** Qué ocurrió. */
  String causa();

  /** Materias, agrupaciones, reglas o enlaces involucrados; puede estar vacía. */
  List<String> elementos();
}
