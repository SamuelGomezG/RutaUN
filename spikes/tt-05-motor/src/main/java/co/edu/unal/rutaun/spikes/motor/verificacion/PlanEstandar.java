package co.edu.unal.rutaun.spikes.motor.verificacion;

import co.edu.unal.rutaun.spikes.motor.busqueda.Plan;
import co.edu.unal.rutaun.spikes.motor.modelo.ModeloDeCalculo;
import java.util.List;

/** Plan de la malla estándar adaptado a los topes del spike y sus mutaciones. */
public final class PlanEstandar {

  private PlanEstandar() {}

  private static final List<List<String>> SEMESTRES =
      List.of(
          List.of("1000004", "2025975", "2015734", "2016703"),
          List.of("1000019", "1000005", "1000003", "2016375"),
          List.of("1000017", "1000006", "2025963", "2016353", "2016698"),
          List.of("1000013", "2015703", "2025964", "2016699", "2016697"),
          List.of("2025970", "2015702", "2025967", "2016701", "2015174"),
          List.of("2025971", "2025982", "2015970", "2016702", "2016696", "2016707"),
          List.of("2025969", "2025983", "2025994", "2016716", "2025995", "2025966"),
          List.of("2024045", "2016722", "2025972", "2025960"),
          List.of(),
          List.of("2016843"));

  private static final int[] BLOQUES = {0, 5, 0, 0, 0, 0, 0, 0, 0, 16, 12};

  public static Plan adaptado(ModeloDeCalculo modelo) {
    int n = modelo.cantidadMaterias();
    int[] semestre = new int[n];
    java.util.Arrays.fill(semestre, -1);
    for (int s = 0; s < SEMESTRES.size(); s++) {
      for (String codigo : SEMESTRES.get(s)) {
        Integer indice = modelo.indiceDe(codigo);
        if (indice == null) {
          throw new IllegalArgumentException("Código del plan estándar desconocido: " + codigo);
        }
        semestre[indice] = s + 1;
      }
    }
    return reconstruir(modelo, semestre, BLOQUES.clone(), modelo.asignacionInicial());
  }

  public static Plan intercambiar(
      ModeloDeCalculo modelo, Plan base, String codigoA, String codigoB) {
    int[] semestre = base.semestrePorMateria().clone();
    int a = indice(modelo, codigoA);
    int b = indice(modelo, codigoB);
    int temporal = semestre[a];
    semestre[a] = semestre[b];
    semestre[b] = temporal;
    return reconstruir(modelo, semestre, base.bloquePorSemestre().clone(), base.asignacion());
  }

  public static Plan quitarYAgregarBloque(
      ModeloDeCalculo modelo, Plan base, String codigoQuitado, int semestre, int creditosExtra) {
    int[] semestrePorMateria = base.semestrePorMateria().clone();
    int[] bloque = base.bloquePorSemestre().clone();
    semestrePorMateria[indice(modelo, codigoQuitado)] = -1;
    bloque[semestre] += creditosExtra;
    return reconstruir(modelo, semestrePorMateria, bloque, base.asignacion());
  }

  public static Plan reconstruir(
      ModeloDeCalculo modelo, int[] semestre, int[] bloque, int[] asignacionBase) {
    int n = modelo.cantidadMaterias();
    int k = 0;
    for (int valor : semestre) {
      k = Math.max(k, valor);
    }
    for (int s = 1; s < bloque.length; s++) {
      if (bloque[s] > 0) {
        k = Math.max(k, s);
      }
    }
    if (bloque.length != k + 1) {
      bloque = java.util.Arrays.copyOf(bloque, k + 1);
    }
    int[] asignacion = asignacionBase.clone();
    int[] creditosPorSemestre = new int[k + 1];
    int marcados = 0;
    int creditosOptativas = 0;
    for (int s = 1; s <= k; s++) {
      creditosPorSemestre[s] = bloque[s];
      if (creditosPorSemestre[s] < modelo.minimoPorPeriodo()) {
        marcados++;
      }
    }
    for (int i = 0; i < n; i++) {
      if (semestre[i] <= 0) {
        continue;
      }
      if (asignacion[i] < 0) {
        int[] grupos = modelo.gruposDeMateria(i);
        if (grupos.length > 0) {
          asignacion[i] = grupos[0];
        }
      }
      creditosPorSemestre[semestre[i]] += modelo.creditos(i);
      if (!modelo.requeridas().contiene(i)) {
        creditosOptativas += modelo.creditos(i);
      }
    }
    marcados = 0;
    for (int s = 1; s <= k; s++) {
      if (creditosPorSemestre[s] < modelo.minimoPorPeriodo()) {
        marcados++;
      }
    }
    return new Plan(
        k, semestre, asignacion, bloque, marcados, creditosOptativas, creditosPorSemestre);
  }

  private static int indice(ModeloDeCalculo modelo, String codigo) {
    Integer indice = modelo.indiceDe(codigo);
    if (indice == null) {
      throw new IllegalArgumentException("Código desconocido: " + codigo);
    }
    return indice;
  }
}
