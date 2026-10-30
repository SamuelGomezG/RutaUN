package co.edu.unal.rutaun.compartido;

import co.edu.unal.rutaun.utilidades.ArgumentChecks;

/** Cantidad de créditos: un entero no negativo que se puede sumar. */
public record Creditos(int valor) {

  /** Rechaza un valor negativo. */
  public Creditos {
    ArgumentChecks.obligatorioNoNegativo(valor, "valor");
  }

  /** Devuelve la suma de estos créditos con los de {@code otros}. */
  public Creditos suma(Creditos otros) {
    return new Creditos(valor + ArgumentChecks.obligatorio(otros, "otros").valor());
  }
}
