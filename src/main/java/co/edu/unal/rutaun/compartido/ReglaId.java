package co.edu.unal.rutaun.compartido;

import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.util.UUID;

/** Identificador tipado de una regla: un UUID generado en el dominio. */
public record ReglaId(UUID valor) {

  /** Rechaza un valor nulo. */
  public ReglaId {
    ArgumentChecks.obligatorio(valor, "valor");
  }

  /** Genera el identificador de una regla nueva. */
  public static ReglaId nuevo() {
    return new ReglaId(UUID.randomUUID());
  }
}
