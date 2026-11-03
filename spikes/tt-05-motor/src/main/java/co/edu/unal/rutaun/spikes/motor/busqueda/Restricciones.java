package co.edu.unal.rutaun.spikes.motor.busqueda;

import co.edu.unal.rutaun.spikes.motor.modelo.ConjuntoDeMaterias;
import java.util.Arrays;

/** Restricciones adicionales para la fijación sucesiva de criterios. */
public final class Restricciones {

  int maxSemestresMarcados = Integer.MAX_VALUE;
  int maxSemestres = Integer.MAX_VALUE;
  int maxCreditosOptativas = Integer.MAX_VALUE;
  boolean soloIncluidas;
  ConjuntoDeMaterias incluidas;
  ConjuntoDeMaterias obligatoriasExtra;
  ConjuntoDeMaterias excluidas;
  int[] semestreFijo;
  int[] semestreMaximo;
  Integer[] bloqueFijo;

  public Restricciones(int cantidadMaterias) {
    incluidas = new ConjuntoDeMaterias(cantidadMaterias);
    obligatoriasExtra = new ConjuntoDeMaterias(cantidadMaterias);
    excluidas = new ConjuntoDeMaterias(cantidadMaterias);
    semestreFijo = new int[cantidadMaterias];
    Arrays.fill(semestreFijo, -1);
    semestreMaximo = new int[cantidadMaterias];
    Arrays.fill(semestreMaximo, Integer.MAX_VALUE);
  }

  public Restricciones copia() {
    Restricciones copia = new Restricciones(semestreFijo.length);
    copia.maxSemestresMarcados = maxSemestresMarcados;
    copia.maxSemestres = maxSemestres;
    copia.maxCreditosOptativas = maxCreditosOptativas;
    copia.soloIncluidas = soloIncluidas;
    copia.incluidas = incluidas.copia();
    copia.obligatoriasExtra = obligatoriasExtra.copia();
    copia.excluidas = excluidas.copia();
    copia.semestreFijo = semestreFijo.clone();
    copia.semestreMaximo = semestreMaximo.clone();
    copia.bloqueFijo = bloqueFijo == null ? null : bloqueFijo.clone();
    return copia;
  }
}
