package co.edu.unal.rutaun.normativa.aplicacion;

import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.compartido.EstadoRegla;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Elige el nombre con que se muestra cada materia (UC05 paso 4). El nombre vive en la regla de
 * materia de cada versión del consolidado y una norma programada puede darle otro; el que se
 * muestra es el de la regla que rige y, si ninguna rige, el de la programada y luego el de la que
 * está en revisión. Con el mismo estado gana la norma expedida más recientemente y, al final, el
 * identificador de la regla, para que el resultado no dependa del orden en que llegan las reglas.
 */
final class NombreDeMateria {

  private NombreDeMateria() {}

  /**
   * El nombre que una regla de materia le da a una materia.
   *
   * @param materia código de la materia
   * @param nombre nombre que la regla le da
   * @param estado estado de la regla en la versión
   * @param expedicion fecha de expedición de la norma de la regla; puede faltar
   * @param regla identificador de la regla
   */
  record Candidato(
      CodigoMateria materia, String nombre, EstadoRegla estado, LocalDate expedicion, UUID regla) {

    /** Exige todos los datos, salvo la fecha de expedición. */
    Candidato {
      ArgumentChecks.obligatorio(materia, "materia");
      ArgumentChecks.obligatorio(nombre, "nombre");
      ArgumentChecks.obligatorio(estado, "estado");
      ArgumentChecks.obligatorio(regla, "regla");
    }
  }

  private static final Comparator<Candidato> PREFERENCIA =
      Comparator.comparingInt((Candidato candidato) -> prioridad(candidato.estado()))
          .thenComparing(Candidato::expedicion, Comparator.nullsLast(Comparator.reverseOrder()))
          .thenComparing(Candidato::regla);

  private static int prioridad(EstadoRegla estado) {
    return switch (estado) {
      case VIGENTE -> 0;
      case PROGRAMADA -> 1;
      case EN_REVISION -> 2;
      case YA_NO_RIGE -> 3;
    };
  }

  /**
   * El nombre de cada materia que algún candidato nombra.
   *
   * @param candidatos las reglas de materia, en cualquier orden
   * @return el nombre elegido por código de materia
   */
  static Map<CodigoMateria, String> elegir(Collection<Candidato> candidatos) {
    ArgumentChecks.obligatorio(candidatos, "candidatos");
    Map<CodigoMateria, Candidato> elegidos = new HashMap<>();
    for (Candidato candidato : candidatos) {
      elegidos.merge(
          candidato.materia(),
          candidato,
          (actual, otro) -> PREFERENCIA.compare(otro, actual) < 0 ? otro : actual);
    }
    Map<CodigoMateria, String> nombres = new HashMap<>();
    elegidos.forEach((materia, candidato) -> nombres.put(materia, candidato.nombre()));
    return nombres;
  }
}
