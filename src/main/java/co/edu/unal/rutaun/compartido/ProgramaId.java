package co.edu.unal.rutaun.compartido;

import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.util.UUID;

/** Identificador tipado de un programa: un UUID generado en el dominio. */
public record ProgramaId(UUID valor) {

  /** Rechaza un valor nulo. */
  public ProgramaId {
    ArgumentChecks.obligatorio(valor, "valor");
  }

  /** Genera el identificador de un programa nuevo. */
  public static ProgramaId nuevo() {
    return new ProgramaId(UUID.randomUUID());
  }
}
