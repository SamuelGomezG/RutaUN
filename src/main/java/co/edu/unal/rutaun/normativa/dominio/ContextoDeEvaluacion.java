package co.edu.unal.rutaun.normativa.dominio;

import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.compartido.Creditos;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.util.Map;
import java.util.Set;

/**
 * Datos con los que se evalúa una restricción (glosario, «Restricción (de una materia)»): el estado
 * de cada materia, los créditos aprobados por medida y las materias que el plan ubica en el mismo
 * semestre o antes. La combinación de los estados de los términos es TT-03-T4.
 *
 * @param estadosDeMaterias estado de las materias que se evalúan
 * @param creditosAprobados créditos aprobados del programa o de cada componente
 * @param materiasEnElMismoSemestre materias que el plan ubica en el mismo semestre o antes
 */
public record ContextoDeEvaluacion(
    Map<CodigoMateria, EstadoDeMateria> estadosDeMaterias,
    Map<MedidaDeCreditos, Creditos> creditosAprobados,
    Set<CodigoMateria> materiasEnElMismoSemestre) {

  /** Exige los mapas y el conjunto, y los deja inmutables. */
  public ContextoDeEvaluacion {
    estadosDeMaterias =
        Map.copyOf(ArgumentChecks.obligatorio(estadosDeMaterias, "estadosDeMaterias"));
    creditosAprobados =
        Map.copyOf(ArgumentChecks.obligatorio(creditosAprobados, "creditosAprobados"));
    materiasEnElMismoSemestre =
        Set.copyOf(
            ArgumentChecks.obligatorio(materiasEnElMismoSemestre, "materiasEnElMismoSemestre"));
  }

  /** Estado de una materia; si no figura en el contexto, está pendiente. */
  public EstadoDeMateria estadoDe(CodigoMateria materia) {
    return estadosDeMaterias.getOrDefault(
        ArgumentChecks.obligatorio(materia, "materia"), EstadoDeMateria.PENDIENTE);
  }

  /** Créditos aprobados para una medida; si no figura en el contexto, son cero. */
  public Creditos creditosAprobadosPara(MedidaDeCreditos medida) {
    return creditosAprobados.getOrDefault(
        ArgumentChecks.obligatorio(medida, "medida"), new Creditos(0));
  }

  /** Indica si el plan ubica la materia en el mismo semestre o antes. */
  public boolean vaEnElMismoSemestre(CodigoMateria materia) {
    return materiasEnElMismoSemestre.contains(ArgumentChecks.obligatorio(materia, "materia"));
  }
}
