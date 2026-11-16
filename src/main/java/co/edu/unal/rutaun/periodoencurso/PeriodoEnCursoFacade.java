package co.edu.unal.rutaun.periodoencurso;

import co.edu.unal.rutaun.compartido.PeriodoAcademico;

/**
 * Periodo en curso (glosario, «Periodo académico»): el último que empezó, según lo registra el
 * administrador de normativa. Quien lo necesita depende de esta interfaz y no de dónde se guarde el
 * valor.
 */
public interface PeriodoEnCursoFacade {

  /** Devuelve el periodo académico en curso. */
  PeriodoAcademico actual();
}
