package co.edu.unal.rutaun.spikes.motor.verificacion;

import co.edu.unal.rutaun.spikes.motor.busqueda.Plan;
import co.edu.unal.rutaun.spikes.motor.busqueda.Tope;
import co.edu.unal.rutaun.spikes.motor.modelo.ConjuntoDeMaterias;
import co.edu.unal.rutaun.spikes.motor.modelo.ModeloDeCalculo;
import java.util.ArrayList;
import java.util.List;

/** Verifica un plan sin usar el código de búsqueda. */
public final class VerificadorDePlan {

  private final ModeloDeCalculo modelo;
  private final int cantidadMaterias;

  public VerificadorDePlan(ModeloDeCalculo modelo) {
    this.modelo = modelo;
    this.cantidadMaterias = modelo.cantidadMaterias();
  }

  public List<String> verificar(Plan plan, ConjuntoDeMaterias a0, int libreInicial, Tope tope) {
    List<String> errores = new ArrayList<>();
    int[] semestre = plan.semestrePorMateria();
    int[] bloque = plan.bloquePorSemestre();
    final int[] asignacion = plan.asignacion();
    int k = plan.k();
    if (bloque.length != k + 1) {
      errores.add("El vector de bloques no mide k + 1");
    }
    ConjuntoDeMaterias cursadas = a0.copia();
    for (int i = 0; i < cantidadMaterias; i++) {
      if (semestre[i] < -1 || semestre[i] > k) {
        errores.add("Semestre fuera de rango para " + modelo.codigo(i) + ": " + semestre[i]);
      }
      if (semestre[i] > 0) {
        if (!modelo.cursable(i)) {
          errores.add("Materia no cursable en el plan: " + modelo.codigo(i));
        }
        if (cursadas.contiene(i)) {
          errores.add("Materia repetida entre avance y plan: " + modelo.codigo(i));
        }
        cursadas.agregar(i);
      }
    }

    int sumaBloques = 0;
    int[] libreAntes = new int[k + 1];
    for (int s = 1; s <= k; s++) {
      libreAntes[s] = libreAntes[s - 1] + (s >= 2 ? bloque[s - 1] : 0);
      int creditos = bloque[s];
      int materias = Math.ceilDiv(bloque[s], modelo.creditosPorMateriaLibre());
      int materiasDelSemestre = 0;
      for (int i = 0; i < cantidadMaterias; i++) {
        if (semestre[i] == s) {
          creditos += modelo.creditos(i);
          materiasDelSemestre++;
        }
      }
      materias += materiasDelSemestre;
      if (creditos == 0) {
        errores.add("Semestre vacío: " + s);
      }
      if (creditos > tope.limiteCreditos()) {
        errores.add("Semestre " + s + " excede el tope de créditos: " + creditos);
      }
      if (materias > tope.limiteMaterias()) {
        errores.add("Semestre " + s + " excede el tope de materias: " + materias);
      }
      if (libreInicial >= modelo.creditosPorMateriaLibre()
          && bloque[s] > 0
          && bloque[s] < modelo.creditosPorMateriaLibre()) {
        errores.add("Bloque de menos de 3 créditos en el semestre " + s);
      }
      sumaBloques += bloque[s];
    }
    if (sumaBloques != libreInicial) {
      errores.add("Los bloques suman " + sumaBloques + " y se esperaban " + libreInicial);
    }
    if (libreInicial < modelo.creditosPorMateriaLibre()) {
      int bloquesNoVacios = 0;
      for (int s = 1; s <= k; s++) {
        if (bloque[s] > 0) {
          bloquesNoVacios++;
          if (bloque[s] != libreInicial) {
            errores.add("Bloque distinto del total con menos de 3 créditos en el semestre " + s);
          }
        }
      }
      if (bloquesNoVacios > 1) {
        errores.add("Hay más de un bloque cuando la libre elección es menor que 3");
      }
    }

    for (int i = 0; i < cantidadMaterias; i++) {
      if (semestre[i] <= 0) {
        continue;
      }
      ConjuntoDeMaterias antes = a0.copia();
      ConjuntoDeMaterias hasta = a0.copia();
      for (int j = 0; j < cantidadMaterias; j++) {
        if (semestre[j] <= 0 || j == i) {
          continue;
        }
        if (semestre[j] < semestre[i]) {
          antes.agregar(j);
        }
        if (semestre[j] <= semestre[i]) {
          hasta.agregar(j);
        }
      }
      if (!modelo.clausulasCumplidas(modelo.prerrequisitos(i), antes)) {
        errores.add("Prerrequisito incumplido: " + modelo.codigo(i));
      }
      if (!modelo.clausulasCumplidas(modelo.correquisitos(i), hasta)) {
        errores.add("Correquisito incumplido: " + modelo.codigo(i));
      }
      if (!modelo.habilitacionesCumplidas(i, antes, libreAntes[semestre[i]], asignacion)) {
        errores.add("Habilitación incumplida: " + modelo.codigo(i));
      }
    }

    long[] palabrasRequeridas = modelo.requeridas().palabras();
    for (int i = 0; i < cantidadMaterias; i++) {
      if ((palabrasRequeridas[i >>> 6] & (1L << (i & 63))) != 0 && !cursadas.contiene(i)) {
        errores.add("Falta una materia requerida: " + modelo.codigo(i));
      }
    }
    if (!modelo.todoCubierto(cursadas, asignacion)) {
      errores.add("Alguna agrupación o componente queda sin cubrir");
    }
    if (!optativasMinimas(cursadas, semestre, bloque, a0, asignacion, libreAntes)) {
      errores.add("Hay una optativa elegida que no es necesaria");
    }
    return errores;
  }

  private boolean optativasMinimas(
      ConjuntoDeMaterias cursadas,
      int[] semestre,
      int[] bloque,
      ConjuntoDeMaterias a0,
      int[] asignacion,
      int[] libreAntes) {
    for (int m = 0; m < cantidadMaterias; m++) {
      if (semestre[m] <= 0 || modelo.requeridas().contiene(m)) {
        continue;
      }
      ConjuntoDeMaterias sinM = cursadas.copia();
      sinM.quitar(m);
      int[] asignacionSinM = asignacion.clone();
      asignacionSinM[m] = -1;
      boolean necesaria = !modelo.todoCubierto(sinM, asignacionSinM);
      if (!necesaria) {
        necesaria = rompeRestricciones(sinM, semestre, asignacionSinM, a0, libreAntes);
      }
      if (!necesaria) {
        return false;
      }
    }
    return true;
  }

  private boolean rompeRestricciones(
      ConjuntoDeMaterias sinM,
      int[] semestre,
      int[] asignacion,
      ConjuntoDeMaterias a0,
      int[] libreAntes) {
    for (int j = 0; j < cantidadMaterias; j++) {
      if (semestre[j] <= 0 || !sinM.contiene(j)) {
        continue;
      }
      ConjuntoDeMaterias antes = a0.copia();
      ConjuntoDeMaterias hasta = a0.copia();
      for (int p = 0; p < cantidadMaterias; p++) {
        if (p == j || semestre[p] <= 0 || !sinM.contiene(p)) {
          continue;
        }
        if (semestre[p] < semestre[j]) {
          antes.agregar(p);
        }
        if (semestre[p] <= semestre[j]) {
          hasta.agregar(p);
        }
      }
      if (!modelo.clausulasCumplidas(modelo.prerrequisitos(j), antes)) {
        return true;
      }
      if (!modelo.habilitacionesCumplidas(j, antes, libreAntes[semestre[j]], asignacion)) {
        return true;
      }
      if (!modelo.clausulasCumplidas(modelo.correquisitos(j), hasta)) {
        return true;
      }
    }
    return false;
  }
}
