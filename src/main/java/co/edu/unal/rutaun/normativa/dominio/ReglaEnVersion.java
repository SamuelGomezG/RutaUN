package co.edu.unal.rutaun.normativa.dominio;

import co.edu.unal.rutaun.compartido.EstadoRegla;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;

/**
 * Una regla y su estado en una versión del consolidado (diseño de clases, «VersionDelConsolidado»).
 * La misma regla puede estar en varias versiones con estados distintos (#152).
 *
 * @param regla regla del consolidado
 * @param estado estado que la regla tiene en esta versión
 */
public record ReglaEnVersion(Regla regla, EstadoRegla estado) {

  /** Exige la regla y su estado en la versión. */
  public ReglaEnVersion {
    ArgumentChecks.obligatorio(regla, "regla");
    ArgumentChecks.obligatorio(estado, "estado");
  }
}
