package co.edu.unal.rutaun.compartido;

import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.util.ArrayList;
import java.util.List;

/**
 * Resultado de evaluar una restricción por certeza (S18): cumplida, en curso, incierta o pendiente,
 * con las razones que lo explican. Se combinan con {@link #y}, que toma el peor estado de sus
 * términos, y con {@link #o}, que toma el mejor.
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

  /**
   * «y» con otro estado: toma el peor de los dos y las razones de los términos no cumplidos (S18).
   */
  public EstadoRestriccion y(EstadoRestriccion otro) {
    ArgumentChecks.obligatorio(otro, "otro");
    List<String> razonesCombinadas = new ArrayList<>();
    if (certeza() != Certeza.CUMPLIDA) {
      razonesCombinadas.addAll(razones());
    }
    if (otro.certeza() != Certeza.CUMPLIDA) {
      razonesCombinadas.addAll(otro.razones());
    }
    if (razonesCombinadas.isEmpty()) {
      razonesCombinadas.addAll(razones());
      razonesCombinadas.addAll(otro.razones());
    }
    return new EstadoRestriccion(peor(certeza(), otro.certeza()), razonesCombinadas);
  }

  /**
   * «o» con otro estado: toma el mejor de los dos y las razones de las ramas que lo tienen (S18).
   */
  public EstadoRestriccion o(EstadoRestriccion otro) {
    ArgumentChecks.obligatorio(otro, "otro");
    Certeza laMejor = mejor(certeza(), otro.certeza());
    List<String> razonesCombinadas = new ArrayList<>();
    if (certeza() == laMejor) {
      razonesCombinadas.addAll(razones());
    }
    if (otro.certeza() == laMejor) {
      razonesCombinadas.addAll(otro.razones());
    }
    return new EstadoRestriccion(laMejor, razonesCombinadas);
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

  private static Certeza peor(Certeza izquierda, Certeza derecha) {
    return izquierda.ordinal() >= derecha.ordinal() ? izquierda : derecha;
  }

  private static Certeza mejor(Certeza izquierda, Certeza derecha) {
    return izquierda.ordinal() <= derecha.ordinal() ? izquierda : derecha;
  }
}
