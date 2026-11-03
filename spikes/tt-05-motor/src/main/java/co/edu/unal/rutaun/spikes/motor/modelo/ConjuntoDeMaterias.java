package co.edu.unal.rutaun.spikes.motor.modelo;

import java.util.Arrays;

/** Conjunto de materias como bitset de {@code long}; no crea objetos en el ciclo caliente. */
public final class ConjuntoDeMaterias {

  private final int cantidad;
  private final long[] palabras;

  public ConjuntoDeMaterias(int cantidad) {
    this.cantidad = cantidad;
    this.palabras = new long[palabrasNecesarias(cantidad)];
  }

  private ConjuntoDeMaterias(int cantidad, long[] palabras) {
    this.cantidad = cantidad;
    this.palabras = palabras;
  }

  public static ConjuntoDeMaterias de(int cantidad, int... indices) {
    ConjuntoDeMaterias conjunto = new ConjuntoDeMaterias(cantidad);
    for (int indice : indices) {
      conjunto.agregar(indice);
    }
    return conjunto;
  }

  public int cantidad() {
    return cantidad;
  }

  public void agregar(int indice) {
    if (indice < 0 || indice >= cantidad) {
      throw new IndexOutOfBoundsException("Índice de materia fuera de rango: " + indice);
    }
    palabras[indice >>> 6] |= 1L << (indice & 63);
  }

  public void quitar(int indice) {
    palabras[indice >>> 6] &= ~(1L << (indice & 63));
  }

  public boolean contiene(int indice) {
    return (palabras[indice >>> 6] & (1L << (indice & 63))) != 0;
  }

  public boolean vacio() {
    for (long palabra : palabras) {
      if (palabra != 0) {
        return false;
      }
    }
    return true;
  }

  public int cardinalidad() {
    int total = 0;
    for (long palabra : palabras) {
      total += Long.bitCount(palabra);
    }
    return total;
  }

  public ConjuntoDeMaterias copia() {
    return new ConjuntoDeMaterias(cantidad, palabras.clone());
  }

  public void copiarDesde(ConjuntoDeMaterias otro) {
    System.arraycopy(otro.palabras, 0, palabras, 0, palabras.length);
  }

  public void unir(ConjuntoDeMaterias otro) {
    for (int i = 0; i < palabras.length; i++) {
      palabras[i] |= otro.palabras[i];
    }
  }

  public void intersectar(ConjuntoDeMaterias otro) {
    for (int i = 0; i < palabras.length; i++) {
      palabras[i] &= otro.palabras[i];
    }
  }

  public boolean contieneAlguna(long[] clausula) {
    for (int i = 0; i < palabras.length; i++) {
      if ((palabras[i] & clausula[i]) != 0) {
        return true;
      }
    }
    return false;
  }

  public boolean contieneTodas(long[] mascara) {
    for (int i = 0; i < palabras.length; i++) {
      if ((palabras[i] & mascara[i]) != mascara[i]) {
        return false;
      }
    }
    return true;
  }

  public long[] palabras() {
    return palabras;
  }

  public long[] copiaPalabras() {
    return palabras.clone();
  }

  public static int palabrasNecesarias(int cantidad) {
    return (cantidad + 63) >>> 6;
  }

  @Override
  public boolean equals(Object objeto) {
    if (this == objeto) {
      return true;
    }
    if (!(objeto instanceof ConjuntoDeMaterias otro)) {
      return false;
    }
    return cantidad == otro.cantidad && Arrays.equals(palabras, otro.palabras);
  }

  @Override
  public int hashCode() {
    return 31 * cantidad + Arrays.hashCode(palabras);
  }

  @Override
  public String toString() {
    return "ConjuntoDeMaterias" + Arrays.toString(palabras);
  }
}
