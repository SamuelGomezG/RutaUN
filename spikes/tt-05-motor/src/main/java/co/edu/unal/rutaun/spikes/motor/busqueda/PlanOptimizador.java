package co.edu.unal.rutaun.spikes.motor.busqueda;

import co.edu.unal.rutaun.spikes.motor.modelo.ConjuntoDeMaterias;
import co.edu.unal.rutaun.spikes.motor.modelo.ModeloDeCalculo;
import co.edu.unal.rutaun.spikes.motor.verificacion.PlanEstandar;
import co.edu.unal.rutaun.spikes.motor.verificacion.VerificadorDePlan;
import java.util.List;

/** Mejora local de un plan válido para acercarse a los criterios 5 y 6. */
public final class PlanOptimizador {

  private PlanOptimizador() {}

  public static Plan optimizarSemestres(
      ModeloDeCalculo modelo,
      ConjuntoDeMaterias a0,
      int libreInicial,
      Tope tope,
      Plan plan,
      int maxMarcados,
      int k,
      Cancelacion cancelacion) {
    int n = modelo.cantidadMaterias();
    int[] semestre = plan.semestrePorMateria().clone();
    int[] bloque = plan.bloquePorSemestre().clone();
    Plan actual = plan;
    for (int materia = 0; materia < n; materia++) {
      if (cancelacion.cancelado()) {
        return null;
      }
      if (semestre[materia] <= 0 || a0.contiene(materia)) {
        continue;
      }
      int original = semestre[materia];
      for (int s = 1; s < original; s++) {
        if (cancelacion.cancelado()) {
          return null;
        }
        semestre[materia] = s;
        Plan candidato = PlanEstandar.reconstruir(modelo, semestre, bloque, actual.asignacion());
        if (candidato.k() <= k
            && candidato.marcados() <= maxMarcados
            && esValido(modelo, candidato, a0, libreInicial, tope)) {
          actual = candidato;
          break;
        }
        semestre[materia] = original;
      }
      semestre = actual.semestrePorMateria().clone();
      bloque = actual.bloquePorSemestre().clone();
    }
    return actual;
  }

  public static Plan optimizarBloques(
      ModeloDeCalculo modelo,
      ConjuntoDeMaterias a0,
      int libreInicial,
      Tope tope,
      Plan plan,
      int maxMarcados,
      Cancelacion cancelacion) {
    Plan actual = plan;
    for (int s = 1; s <= actual.k(); s++) {
      if (cancelacion.cancelado()) {
        return null;
      }
      int[] bloqueActual = actual.bloquePorSemestre().clone();
      int actualBloque = bloqueActual[s];
      int maximo = Math.min(libreInicial, tope.limiteCreditos());
      for (int b = maximo; b > actualBloque; b--) {
        if (cancelacion.cancelado()) {
          return null;
        }
        int creditosPorMateria = modelo.creditosPorMateriaLibre();
        if (libreInicial >= creditosPorMateria && b > 0 && b < creditosPorMateria) {
          continue;
        }
        if (libreInicial < 3 && b > 0 && b != libreInicial) {
          continue;
        }
        int[] nuevo = bloqueActual.clone();
        int delta = b - actualBloque;
        for (int t = actual.k(); t > s && delta > 0; t--) {
          int disponible = nuevo[t];
          if (disponible <= 0) {
            continue;
          }
          int mover = Math.min(delta, disponible);
          if (disponible - mover > 0 && disponible - mover < modelo.creditosPorMateriaLibre()) {
            mover = disponible - modelo.creditosPorMateriaLibre();
          }
          if (mover <= 0) {
            continue;
          }
          nuevo[t] -= mover;
          delta -= mover;
        }
        if (delta != 0) {
          continue;
        }
        nuevo[s] = b;
        Plan candidato =
            PlanEstandar.reconstruir(
                modelo, actual.semestrePorMateria().clone(), nuevo, actual.asignacion());
        if (candidato.marcados() <= maxMarcados
            && esValido(modelo, candidato, a0, libreInicial, tope)) {
          actual = candidato;
          break;
        }
      }
    }
    return actual;
  }

  private static boolean esValido(
      ModeloDeCalculo modelo, Plan plan, ConjuntoDeMaterias a0, int libreInicial, Tope tope) {
    List<String> errores = new VerificadorDePlan(modelo).verificar(plan, a0, libreInicial, tope);
    return errores.isEmpty();
  }
}
