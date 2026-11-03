package co.edu.unal.rutaun.normativa.dominio;

import co.edu.unal.rutaun.compartido.CodigoMateria;

/**
 * Restricción que el programa exige para cursar una materia (glosario, «Restricción (de una
 * materia)»): un prerrequisito, un correquisito o una regla de habilitación. Las agrupaciones y la
 * carga por periodo no son restricciones de una materia.
 */
public sealed interface Restriccion permits Prerrequisito, Correquisito, ReglaDeHabilitacion {

  /** Materia a la que se refiere la restricción. */
  CodigoMateria materia();
}
