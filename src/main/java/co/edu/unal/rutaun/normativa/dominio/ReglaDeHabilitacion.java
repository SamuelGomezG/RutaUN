package co.edu.unal.rutaun.normativa.dominio;

import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.compartido.Creditos;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;

/**
 * Condición distinta de un prerrequisito: haber aprobado un número mínimo de créditos de un
 * componente o del programa (glosario, «Regla de habilitación»).
 *
 * @param materia materia que exige la habilitación
 * @param medida créditos que se miden: del programa o de un componente
 * @param umbral créditos que se deben aprobar
 */
public record ReglaDeHabilitacion(CodigoMateria materia, MedidaDeCreditos medida, Creditos umbral)
    implements Restriccion {

  /** Exige la materia, la medida y el umbral. */
  public ReglaDeHabilitacion {
    ArgumentChecks.obligatorio(materia, "materia");
    ArgumentChecks.obligatorio(medida, "medida");
    ArgumentChecks.obligatorio(umbral, "umbral");
  }
}
