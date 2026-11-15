package co.edu.unal.rutaun.normativa.aplicacion;

import java.util.Comparator;

/**
 * Orden de los artículos de una norma tal como se rotulan: compara los números como números, así
 * «art. 2» queda antes de «art. 10», y el resto del texto como texto.
 *
 * <p>UC06-HU01-T4 (#132). Sin esto, el orden de texto pondría «art. 10» antes de «art. 2».
 */
final class OrdenDeArticulos implements Comparator<String> {

  /** El orden de los artículos. */
  static final OrdenDeArticulos INSTANCIA = new OrdenDeArticulos();

  private OrdenDeArticulos() {}

  @Override
  public int compare(String a, String b) {
    int i = 0;
    int j = 0;
    while (i < a.length() && j < b.length()) {
      if (Character.isDigit(a.charAt(i)) && Character.isDigit(b.charAt(j))) {
        int finA = finDeNumero(a, i);
        int finB = finDeNumero(b, j);
        int comparacion = compararNumeros(a.substring(i, finA), b.substring(j, finB));
        if (comparacion != 0) {
          return comparacion;
        }
        i = finA;
        j = finB;
      } else {
        int comparacion = Character.compare(a.charAt(i), b.charAt(j));
        if (comparacion != 0) {
          return comparacion;
        }
        i++;
        j++;
      }
    }
    return Integer.compare(a.length() - i, b.length() - j);
  }

  private static int finDeNumero(String texto, int desde) {
    int fin = desde;
    while (fin < texto.length() && Character.isDigit(texto.charAt(fin))) {
      fin++;
    }
    return fin;
  }

  /** Compara dos números dados como texto, sin límite de tamaño: sin ceros a la izquierda. */
  private static int compararNumeros(String a, String b) {
    String sinCerosA = a.replaceFirst("^0+(?=.)", "");
    String sinCerosB = b.replaceFirst("^0+(?=.)", "");
    if (sinCerosA.length() != sinCerosB.length()) {
      return Integer.compare(sinCerosA.length(), sinCerosB.length());
    }
    return sinCerosA.compareTo(sinCerosB);
  }
}
