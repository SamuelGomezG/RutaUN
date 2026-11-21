package co.edu.unal.rutaun.normativa.dominio;

import co.edu.unal.rutaun.compartido.ProgramaId;
import java.util.List;

/**
 * Puente solo de pruebas para armar un {@link ConsolidadoValidado} sin pasar por el {@link
 * ValidadorDelConsolidado}. Lo usan las demostraciones que crean versiones que no son una
 * incorporación (#154). Producción no lo ve.
 */
public final class ConsolidadosDePrueba {

  private ConsolidadosDePrueba() {}

  /** Reúne las reglas ya validadas de un programa. */
  public static ConsolidadoValidado con(ProgramaId programa, List<ReglaEnVersion> reglas) {
    return new ConsolidadoValidado(programa, reglas);
  }
}
