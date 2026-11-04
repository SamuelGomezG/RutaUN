package co.edu.unal.rutaun.normativa.dominio;

import co.edu.unal.rutaun.compartido.ProgramaId;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.util.List;

/**
 * Las reglas de un programa ya validadas: lo único con lo que se crea una versión nueva del
 * consolidado (diseño de clases, «ConsolidadoValidado»). Su constructor es visible solo dentro del
 * paquete, para que únicamente el validador del consolidado pueda construirlo y sea imposible crear
 * una versión con reglas sin validar.
 */
public final class ConsolidadoValidado {

  private final ProgramaId programa;
  private final List<Regla> reglas;

  /** Reúne las reglas validadas de un programa; solo lo usa el validador. */
  ConsolidadoValidado(ProgramaId programa, List<Regla> reglas) {
    this.programa = ArgumentChecks.obligatorio(programa, "programa");
    this.reglas = ArgumentChecks.copiaSinNulos(reglas, "reglas");
  }

  /**
   * Reúne reglas que ya pasaron la validación de UC07-HU03-T3. Es la fábrica que usa la importación
   * mientras no exista el validador completo del consolidado.
   *
   * @param programa programa al que pertenecen las reglas
   * @param reglas reglas ya validadas
   * @return consolidado validado
   */
  public static ConsolidadoValidado paraImportacion(ProgramaId programa, List<Regla> reglas) {
    return new ConsolidadoValidado(programa, reglas);
  }

  /** Programa al que pertenecen las reglas. */
  public ProgramaId programa() {
    return programa;
  }

  /** Reglas validadas, cada una con su estado; la lista no se puede modificar. */
  public List<Regla> reglas() {
    return reglas;
  }
}
