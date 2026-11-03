package co.edu.unal.rutaun.normativa.dominio;

import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.util.List;

/**
 * Resultado de evaluar una restricción por certeza (S18): cumplida, en curso, incierta o pendiente,
 * con las razones que lo explican. Una «y» toma el peor estado de sus términos y una «o», el mejor;
 * esa combinación es TT-03-T4.
 *
 * @param certeza estado de la restricción
 * @param razones explicaciones del estado
 */
public record EstadoRestriccion(Certeza certeza, List<String> razones) {

  /** Estados de una restricción, del más al menos cierto (S18). */
  public enum Certeza {
    /** Se cumple con materias aprobadas. */
    CUMPLIDA,
    /** Solo se cumple contando materias en curso. */
    EN_CURSO,
    /** Algún término ya no rige sin equivalencia que lo traduzca. */
    INCIERTA,
    /** No se cumple. */
    PENDIENTE
  }

  /** Exige la certeza y deja las razones inmutables. */
  public EstadoRestriccion {
    ArgumentChecks.obligatorio(certeza, "certeza");
    razones = ArgumentChecks.copiaSinNulos(razones, "razones");
  }

  /** Estado cumplido con sus razones. */
  public static EstadoRestriccion cumplida(String... razones) {
    return new EstadoRestriccion(Certeza.CUMPLIDA, List.of(razones));
  }

  /** Estado en curso con sus razones. */
  public static EstadoRestriccion enCurso(String... razones) {
    return new EstadoRestriccion(Certeza.EN_CURSO, List.of(razones));
  }

  /** Estado incierto con sus razones. */
  public static EstadoRestriccion incierta(String... razones) {
    return new EstadoRestriccion(Certeza.INCIERTA, List.of(razones));
  }

  /** Estado pendiente con sus razones. */
  public static EstadoRestriccion pendiente(String... razones) {
    return new EstadoRestriccion(Certeza.PENDIENTE, List.of(razones));
  }
}
