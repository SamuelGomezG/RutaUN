package co.edu.unal.rutaun.normativa.dominio;

import co.edu.unal.rutaun.compartido.Creditos;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.util.Optional;

/**
 * Créditos que el plan de estudios exige a un componente o a una agrupación y, si la norma lo dice,
 * cuántos son de obligatorias y cuántos de optativas. Es lo común de los nodos de la malla: vive
 * aquí y no en una clase base.
 *
 * @param exigidos créditos exigidos, incluidos los de todo lo que el nodo contiene
 * @param desglose créditos de obligatorias y de optativas; vacío si la norma no los separa
 */
public record ExigenciaCreditos(Creditos exigidos, Optional<Desglose> desglose) {

  /**
   * Créditos exigidos en obligatorias y en optativas.
   *
   * @param obligatorios créditos de materias obligatorias
   * @param optativos créditos de materias optativas
   */
  public record Desglose(Creditos obligatorios, Creditos optativos) {

    /** Exige ambos valores. */
    public Desglose {
      ArgumentChecks.obligatorio(obligatorios, "obligatorios");
      ArgumentChecks.obligatorio(optativos, "optativos");
    }
  }

  /** Exige los créditos y, si hay desglose, que sus partes sumen lo exigido. */
  public ExigenciaCreditos {
    ArgumentChecks.obligatorio(exigidos, "exigidos");
    ArgumentChecks.obligatorio(desglose, "desglose");
    if (desglose.isPresent()
        && !desglose.get().obligatorios().suma(desglose.get().optativos()).equals(exigidos)) {
      throw new IllegalArgumentException("el desglose debe sumar los créditos exigidos");
    }
  }

  /** Exigencia sin desglose. */
  public static ExigenciaCreditos de(Creditos exigidos) {
    return new ExigenciaCreditos(exigidos, Optional.empty());
  }

  /** Exigencia con su desglose en obligatorias y optativas; los exigidos son la suma. */
  public static ExigenciaCreditos conDesglose(Creditos obligatorios, Creditos optativos) {
    Desglose desglose = new Desglose(obligatorios, optativos);
    return new ExigenciaCreditos(
        desglose.obligatorios().suma(desglose.optativos()), Optional.of(desglose));
  }
}
