package co.edu.unal.rutaun.normativa.dominio;

import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.compartido.EstadoRestriccion;
import co.edu.unal.rutaun.normativa.ContextoDeEvaluacion;

/**
 * Restricción que el programa exige para cursar una materia (glosario, «Restricción (de una
 * materia)»): un prerrequisito, un correquisito o una regla de habilitación. Las agrupaciones y la
 * carga por periodo no son restricciones de una materia.
 */
public sealed interface Restriccion permits Prerrequisito, Correquisito, ReglaDeHabilitacion {

  /** Materia a la que se refiere la restricción. */
  CodigoMateria materia();

  /**
   * Evalúa la restricción contra el contexto y devuelve su certeza con las razones que la explican
   * (S18).
   *
   * @param contexto lo que la restricción necesita saber para evaluarse
   * @return estado de la restricción
   */
  EstadoRestriccion evaluar(ContextoDeEvaluacion contexto);
}
