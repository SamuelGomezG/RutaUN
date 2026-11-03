package co.edu.unal.rutaun.normativa.dominio;

import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.util.Optional;

/**
 * Medida de una regla de habilitación (glosario, «Regla de habilitación»): créditos aprobados del
 * programa o de un componente. El componente se identifica por su nombre, que es un dato de la
 * norma; el programa no lleva nombre porque la regla ya pertenece a un programa.
 */
public record MedidaDeCreditos(Optional<String> componente) {

  /** Exige el componente, si lo hay, y rechaza un nombre vacío. */
  public MedidaDeCreditos {
    ArgumentChecks.obligatorio(componente, "componente")
        .ifPresent(nombre -> ArgumentChecks.obligatorioNoVacio(nombre, "componente"));
  }

  /** Medida sobre los créditos aprobados del programa. */
  public static MedidaDeCreditos delPrograma() {
    return new MedidaDeCreditos(Optional.empty());
  }

  /** Medida sobre los créditos aprobados de un componente. */
  public static MedidaDeCreditos deComponente(String nombre) {
    return new MedidaDeCreditos(
        Optional.of(ArgumentChecks.obligatorioNoVacio(nombre, "componente")));
  }

  /** Indica si la medida es sobre el programa y no sobre un componente. */
  public boolean esDelPrograma() {
    return componente.isEmpty();
  }
}
