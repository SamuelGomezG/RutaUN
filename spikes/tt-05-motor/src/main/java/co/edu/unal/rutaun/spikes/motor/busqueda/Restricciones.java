package co.edu.unal.rutaun.spikes.motor.busqueda;

import co.edu.unal.rutaun.spikes.motor.modelo.ConjuntoDeMaterias;

/** Restricciones adicionales para la fijación sucesiva de criterios. */
public final class Restricciones {

  public int maxSemestresMarcados = Integer.MAX_VALUE;
  public int maxSemestres = Integer.MAX_VALUE;
  public int maxCreditosOptativas = Integer.MAX_VALUE;
  public boolean soloIncluidas;
  public ConjuntoDeMaterias incluidas;
  public ConjuntoDeMaterias obligatoriasExtra;
  public ConjuntoDeMaterias excluidas;
  public int[] semestreFijo;
  public int[] semestreMaximo;
  public Integer[] bloqueFijo;

  public Restricciones(int cantidadMaterias) {
    incluidas = new ConjuntoDeMaterias(cantidadMaterias);
    obligatoriasExtra = new ConjuntoDeMaterias(cantidadMaterias);
    excluidas = new ConjuntoDeMaterias(cantidadMaterias);
    semestreFijo = new int[cantidadMaterias];
    java.util.Arrays.fill(semestreFijo, -1);
    semestreMaximo = new int[cantidadMaterias];
    java.util.Arrays.fill(semestreMaximo, Integer.MAX_VALUE);
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
