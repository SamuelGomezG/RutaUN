package co.edu.unal.rutaun.normativa;

import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.compartido.Creditos;
import co.edu.unal.rutaun.compartido.EstadoRestriccion;

/**
 * Lo que una restricción necesita saber para evaluarse (diseño, «Contexto Normativa»). La define
 * Normativa y la implementan AvanceInterpretado (Avance) y PlanEnConstruccion (Plan de carrera).
 */
public interface ContextoDeEvaluacion {

  /**
   * Certeza con que el término se cumple, con su razón estructurada. Quien implementa aplica antes
   * las equivalencias: una materia que ya no rige sin equivalencia que la traduzca es INCIERTA. La
   * razón nunca nombra una materia externa (S17).
   *
   * @param materia materia del término que se evalúa
   * @return estado del término con las razones que lo explican
   */
  EstadoRestriccion estadoDe(CodigoMateria materia);

  /**
   * Créditos aprobados de un componente.
   *
   * @param componente componente que se mide
   * @return créditos aprobados; cero si no hay ninguno
   */
  Creditos creditosAprobadosDelComponente(TipoComponente componente);

  /**
   * Créditos aprobados del programa.
   *
   * @return créditos aprobados; cero si no hay ninguno
   */
  Creditos creditosAprobadosDelPrograma();

  /**
   * Correquisitos: si el plan ubica la materia en el mismo semestre o antes; en el avance, falso.
   *
   * @param materia materia del correquisito
   * @return {@code true} si el plan la ubica en el mismo semestre o antes
   */
  boolean enElMismoSemestre(CodigoMateria materia);
}
