package co.edu.unal.rutaun.compartido;

import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.util.UUID;

/** Identificador tipado de una norma: un UUID generado en el dominio. */
public record NormaId(UUID valor) {

  /** Rechaza un valor nulo. */
  public NormaId {
    ArgumentChecks.obligatorio(valor, "valor");
  }

  /** Genera el identificador de una norma nueva. */
  public static NormaId nuevo() {
    return new NormaId(UUID.randomUUID());
  }
}
