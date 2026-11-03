package co.edu.unal.rutaun.spikes.motor.busqueda;

import java.util.Arrays;

/** Plan encontrado: semestre de cada materia, bloque de libre elección y métricas de desempate. */
public final class Plan {

  private final int k;
  private final int[] semestrePorMateria;
  private final int[] asignacion;
  private final int[] bloquePorSemestre;
  private final int marcados;
  private final int creditosOptativas;
  private final int[] creditosPorSemestre;

  public Plan(
      int k,
      int[] semestrePorMateria,
      int[] asignacion,
      int[] bloquePorSemestre,
      int marcados,
      int creditosOptativas,
      int[] creditosPorSemestre) {
    this.k = k;
    this.semestrePorMateria = semestrePorMateria.clone();
    this.asignacion = asignacion.clone();
    this.bloquePorSemestre = bloquePorSemestre.clone();
    this.marcados = marcados;
    this.creditosOptativas = creditosOptativas;
    this.creditosPorSemestre = creditosPorSemestre.clone();
  }

  public int k() {
    return k;
  }

  public int[] semestrePorMateria() {
    return semestrePorMateria;
  }

  public int[] asignacion() {
    return asignacion;
  }

  public int[] bloquePorSemestre() {
    return bloquePorSemestre;
  }

  public int marcados() {
    return marcados;
  }

  public int creditosOptativas() {
    return creditosOptativas;
  }

  public int semestre(int materia) {
    return semestrePorMateria[materia];
  }

  /** Huella estable del plan para las pruebas de determinismo. */
  public int huella() {
    int resultado = 31 * k + Arrays.hashCode(semestrePorMateria);
    resultado = 31 * resultado + Arrays.hashCode(bloquePorSemestre);
    resultado = 31 * resultado + Arrays.hashCode(asignacion);
    resultado = 31 * resultado + marcados;
    return 31 * resultado + creditosOptativas;
  }
}
