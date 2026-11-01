package co.edu.unal.rutaun.compartido;

/**
 * Parte del año a la que pertenece un periodo académico: la primera o la segunda (el «1» o el «2»
 * de «AAAA-1» y «AAAA-2»).
 */
public enum ParteDelAnio {
  /** Primer periodo del año («AAAA-1»). */
  PRIMERA(1),
  /** Segundo periodo del año («AAAA-2»). */
  SEGUNDA(2);

  private final int numero;

  ParteDelAnio(int numero) {
    this.numero = numero;
  }

  /** Número con que se escribe en el periodo: 1 o 2. */
  public int numero() {
    return numero;
  }
}
