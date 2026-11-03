package co.edu.unal.rutaun.spikes.motor.busqueda;

import co.edu.unal.rutaun.spikes.motor.modelo.ConjuntoDeMaterias;
import co.edu.unal.rutaun.spikes.motor.modelo.ModeloDeCalculo;
import co.edu.unal.rutaun.spikes.motor.modelo.ModeloDeCalculo.Habilitacion;
import java.util.Arrays;

/** Cotas inferiores de semestres para podar la búsqueda. */
public final class Cotas {

  private final ModeloDeCalculo modelo;
  private final Tope tope;
  private final int libreInicial;
  private final ConjuntoDeMaterias requeridas;
  private final ConjuntoDeMaterias extraRequeridas;

  public Cotas(
      ModeloDeCalculo modelo, Tope tope, int libreInicial, ConjuntoDeMaterias extraRequeridas) {
    this.modelo = modelo;
    this.tope = tope;
    this.libreInicial = libreInicial;
    this.requeridas = modelo.requeridas();
    this.extraRequeridas = extraRequeridas;
  }

  public int cotaInferior(ConjuntoDeMaterias D, int L, int[] asignacion) {
    int creditosPendientes = 0;
    int materiasPendientes = 0;
    int cadena = 0;
    int[] memo = new int[modelo.cantidadMaterias()];
    Arrays.fill(memo, -1);
    int habilitaciones = 0;
    long[] palabras = requeridas.palabras();
    for (int i = 0; i < modelo.cantidadMaterias(); i++) {
      if ((palabras[i >>> 6] & (1L << (i & 63))) == 0 || D.contiene(i)) {
        continue;
      }
      creditosPendientes += modelo.creditos(i);
      materiasPendientes++;
      int profundidad = modelo.profundidad(i, D, memo);
      if (profundidad >= ModeloDeCalculo.INFINITO) {
        return ModeloDeCalculo.INFINITO;
      }
      cadena = Math.max(cadena, profundidad);
      for (Habilitacion habilitacion : modelo.habilitaciones(i)) {
        int actual = modelo.creditosMedida(habilitacion.medida(), D, libreInicial - L, asignacion);
        if (actual < habilitacion.umbral()) {
          int extra = habilitacion.umbral() - actual;
          int semestres =
              tope.tieneLimiteCreditos() ? 1 + (int) ceilDiv(extra, tope.limiteCreditos()) : 1;
          habilitaciones = Math.max(habilitaciones, semestres);
        }
      }
    }

    long creditosOptativos = 0;
    long materiasOptativas = 0;
    for (ModeloDeCalculo.Grupo grupo : modelo.grupos()) {
      int minCreditos =
          modelo.minimoCreditosOptativos(grupo.indice(), D, asignacion, extraRequeridas);
      int minMaterias =
          modelo.minimoMateriasOptativas(grupo.indice(), D, asignacion, extraRequeridas);
      if (minCreditos >= ModeloDeCalculo.INFINITO || minMaterias >= ModeloDeCalculo.INFINITO) {
        return ModeloDeCalculo.INFINITO;
      }
      creditosOptativos += minCreditos;
      materiasOptativas += minMaterias;
    }

    long totalCreditos = creditosPendientes + creditosOptativos + L;
    long totalMaterias = materiasPendientes + materiasOptativas + ceilDiv(L, 3);
    int cotaCreditos =
        tope.tieneLimiteCreditos() ? (int) ceilDiv(totalCreditos, tope.limiteCreditos()) : 0;
    int cotaMaterias =
        tope.tieneLimiteMaterias() ? (int) ceilDiv(totalMaterias, tope.limiteMaterias()) : 0;
    return Math.max(Math.max(cadena, habilitaciones), Math.max(cotaCreditos, cotaMaterias));
  }

  private static long ceilDiv(long numerador, long denominador) {
    if (numerador <= 0) {
      return 0;
    }
    return (numerador + denominador - 1) / denominador;
  }
}
