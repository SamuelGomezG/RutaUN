package co.edu.unal.rutaun.compartido;

import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.util.UUID;

/** Identificador tipado de una agrupación: un UUID generado en el dominio. */
public record AgrupacionId(UUID valor) {

  /** Rechaza un valor nulo. */
  public AgrupacionId {
    ArgumentChecks.obligatorio(valor, "valor");
  }

  /** Genera el identificador de una agrupación nueva. */
  public static AgrupacionId nuevo() {
    return new AgrupacionId(UUID.randomUUID());
  }
}
