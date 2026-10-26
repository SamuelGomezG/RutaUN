package co.edu.unal.rutaun.utilidades;

/**
 * Comprobaciones genéricas de argumentos para los constructores de valores y entidades: no nulo, no
 * vacío y no negativo. Falla con {@link IllegalArgumentException}.
 */
public final class ArgumentChecks {

  private ArgumentChecks() {}

  /** Devuelve el valor si no es null; si lo es, falla con el nombre del argumento. */
  public static <T> T obligatorio(T valor, String nombre) {
    if (valor == null) {
      throw new IllegalArgumentException(nombre + " no puede ser null");
    }
    return valor;
  }

  /** Devuelve la cadena si no es null ni vacía o de espacios; si lo es, falla. */
  public static String obligatorioNoVacio(String valor, String nombre) {
    obligatorio(valor, nombre);
    if (valor.isBlank()) {
      throw new IllegalArgumentException(nombre + " no puede ser vacío");
    }
    return valor;
  }

  /** Devuelve el número si no es negativo; si lo es, falla. */
  public static int obligatorioNoNegativo(int valor, String nombre) {
    if (valor < 0) {
      throw new IllegalArgumentException(nombre + " no puede ser negativo");
    }
    return valor;
  }
}
