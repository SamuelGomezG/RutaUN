package co.edu.unal.rutaun.normativa.dominio;

import co.edu.unal.rutaun.normativa.TipoComponente;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.util.Optional;

/**
 * Medida de una regla de habilitación (glosario, «Regla de habilitación»): créditos aprobados del
 * programa o de un componente. El componente se identifica por su {@link TipoComponente}, que es lo
 * que el contexto de evaluación sabe medir; el programa no lleva componente porque la regla ya
 * pertenece a un programa.
 */
public record MedidaDeCreditos(Optional<TipoComponente> componente) {

  /** Exige el componente, si lo hay. */
  public MedidaDeCreditos {
    ArgumentChecks.obligatorio(componente, "componente");
  }

  /** Medida sobre los créditos aprobados del programa. */
  public static MedidaDeCreditos delPrograma() {
    return new MedidaDeCreditos(Optional.empty());
  }

  /** Medida sobre los créditos aprobados de un componente. */
  public static MedidaDeCreditos deComponente(TipoComponente tipo) {
    return new MedidaDeCreditos(Optional.of(ArgumentChecks.obligatorio(tipo, "componente")));
  }

  /** Indica si la medida es sobre el programa y no sobre un componente. */
  public boolean esDelPrograma() {
    return componente.isEmpty();
  }
}
