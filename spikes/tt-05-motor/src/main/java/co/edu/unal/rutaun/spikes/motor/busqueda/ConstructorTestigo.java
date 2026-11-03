package co.edu.unal.rutaun.spikes.motor.busqueda;

import co.edu.unal.rutaun.spikes.motor.modelo.ConjuntoDeMaterias;
import co.edu.unal.rutaun.spikes.motor.modelo.ModeloDeCalculo;
import co.edu.unal.rutaun.spikes.motor.verificacion.PlanEstandar;
import co.edu.unal.rutaun.spikes.motor.verificacion.VerificadorDePlan;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Construye un plan testigo de forma codiciosa. No sustituye a la búsqueda: si el testigo es
 * válido, sirve como prueba de existencia y la búsqueda no recorre el árbol.
 */
public final class ConstructorTestigo {

  private ConstructorTestigo() {}

  public static Plan construir(
      ModeloDeCalculo modelo,
      ConjuntoDeMaterias a0,
      int libreInicial,
      Tope tope,
      Restricciones restricciones,
      Cancelacion cancelacion) {
    if (cancelacion.cancelado()) {
      return null;
    }
    int n = modelo.cantidadMaterias();
    ConjuntoDeMaterias D = a0.copia();
    int[] asignacion = modelo.asignacionInicial();
    for (int i = 0; i < n; i++) {
      if ((D.contiene(i) || restricciones.obligatoriasExtra.contiene(i)) && asignacion[i] < 0) {
        int[] grupos = modelo.gruposDeMateria(i);
        if (grupos.length > 0) {
          asignacion[i] = grupos[0];
        }
      }
    }
    int[] semestre = new int[n];
    Arrays.fill(semestre, -1);
    int maximoSemestres =
        Math.min(
            restricciones.maxSemestres,
            Math.min(200, modelo.cantidadMaterias() + libreInicial + 1));
    if (maximoSemestres < 1) {
      return null;
    }
    int[] bloque = new int[maximoSemestres + 1];
    int[] creditosPorSemestre = new int[maximoSemestres + 1];
    int L = libreInicial;
    int marcados = 0;
    int creditosOptativas = 0;
    int k = 0;

    for (int s = 1; s <= maximoSemestres; s++) {
      if (cancelacion.cancelado()) {
        return null;
      }
      if (L == 0
          && modelo.todoCubierto(D, asignacion)
          && todasRequeridas(D, modelo, restricciones)) {
        k = s - 1;
        break;
      }
      for (int i = 0; i < n; i++) {
        if (restricciones.semestreFijo[i] >= 0
            && restricciones.semestreFijo[i] < s
            && !D.contiene(i)) {
          return null;
        }
      }

      long[] utiles = clausulasUtiles(modelo, D, restricciones);
      List<Integer> requeridos = new ArrayList<>();
      List<Integer> optativas = new ArrayList<>();
      for (int i = 0; i < n; i++) {
        if (cancelacion.cancelado()) {
          return null;
        }
        if (D.contiene(i) || !modelo.cursable(i) || restricciones.excluidas.contiene(i)) {
          continue;
        }
        if (restricciones.semestreFijo[i] >= 0 && restricciones.semestreFijo[i] != s) {
          continue;
        }
        if (restricciones.semestreMaximo[i] < s) {
          continue;
        }
        if (!modelo.clausulasCumplidas(modelo.prerrequisitos(i), D)) {
          continue;
        }
        if (!modelo.habilitacionesCumplidas(i, D, libreInicial - L, asignacion)) {
          continue;
        }
        if (esRequerida(i, modelo, restricciones)) {
          requeridos.add(i);
        } else {
          if (restricciones.soloIncluidas && !restricciones.incluidas.contiene(i)) {
            continue;
          }
          boolean porAgrupacion = false;
          for (int grupo : modelo.gruposDeMateria(i)) {
            if (modelo.faltan(grupo, D, asignacion, restricciones.obligatoriasExtra) > 0) {
              porAgrupacion = true;
              break;
            }
          }
          boolean util = (utiles[i >>> 6] & (1L << (i & 63))) != 0;
          if (porAgrupacion || util) {
            optativas.add(i);
          }
        }
      }
      requeridos.sort(Integer::compareTo);
      optativas.sort(Comparator.comparingInt(modelo::creditos).thenComparingInt(i -> i));

      int creditos = 0;
      int materias = 0;
      List<Integer> seleccion = new ArrayList<>();
      for (int materia : requeridos) {
        if (restricciones.semestreFijo[materia] == s
            && !cabe(modelo, materia, creditos, materias, tope)) {
          return null;
        }
        if (cabe(modelo, materia, creditos, materias, tope)) {
          seleccion.add(materia);
          creditos += modelo.creditos(materia);
          materias++;
        }
      }
      for (int materia : seleccion) {
        int[] gruposMateria = modelo.gruposDeMateria(materia);
        if (asignacion[materia] < 0 && gruposMateria.length > 0) {
          int mejor = gruposMateria[0];
          int mejorFaltan = -1;
          for (int grupo : gruposMateria) {
            int faltan = modelo.faltan(grupo, D, asignacion, restricciones.obligatoriasExtra);
            if (faltan > mejorFaltan) {
              mejorFaltan = faltan;
              mejor = grupo;
            }
          }
          asignacion[materia] = mejor;
        }
        D.agregar(materia);
      }
      for (int materia : optativas) {
        if (cancelacion.cancelado()) {
          return null;
        }
        if (!cabe(modelo, materia, creditos, materias, tope)) {
          continue;
        }
        if (!sigueSiendoUtil(modelo, materia, D, asignacion, restricciones)) {
          continue;
        }
        seleccion.add(materia);
        creditos += modelo.creditos(materia);
        materias++;
        int[] gruposMateria = modelo.gruposDeMateria(materia);
        if (asignacion[materia] < 0 && gruposMateria.length > 0) {
          int mejor = gruposMateria[0];
          int mejorFaltan = -1;
          for (int grupo : gruposMateria) {
            int faltan = modelo.faltan(grupo, D, asignacion, restricciones.obligatoriasExtra);
            if (faltan > mejorFaltan) {
              mejorFaltan = faltan;
              mejor = grupo;
            }
          }
          asignacion[materia] = mejor;
        }
        D.agregar(materia);
      }
      for (int materia : seleccion) {
        semestre[materia] = s;
        if (!modelo.requeridas().contiene(materia)) {
          creditosOptativas += modelo.creditos(materia);
        }
      }
      for (int materia : requeridos) {
        if (!D.contiene(materia) && restricciones.semestreFijo[materia] == s) {
          return null;
        }
      }
      for (int i = 0; i < n; i++) {
        if (restricciones.semestreMaximo[i] == s && !D.contiene(i)) {
          return null;
        }
      }

      int b = bloquePermitido(restricciones, s, L, creditos, materias, tope, libreInicial);
      if (b < 0) {
        return null;
      }
      L -= b;
      bloque[s] = b;
      creditosPorSemestre[s] = creditos + b;
      boolean marcado = creditosPorSemestre[s] < modelo.minimoPorPeriodo();
      if (marcado) {
        marcados++;
      }
      if (creditos == 0 && b == 0) {
        return null;
      }
      k = s;
    }
    if (L != 0
        || !modelo.todoCubierto(D, asignacion)
        || !todasRequeridas(D, modelo, restricciones)) {
      return null;
    }
    redistribuirBloques(
        modelo, semestre, creditosPorSemestre, bloque, restricciones, tope, k, libreInicial);
    marcados = 0;
    for (int s = 1; s <= k; s++) {
      if (creditosPorSemestre[s] < modelo.minimoPorPeriodo()) {
        marcados++;
      }
    }
    if (marcados > restricciones.maxSemestresMarcados
        || creditosOptativas > restricciones.maxCreditosOptativas) {
      return null;
    }
    Plan plan =
        PlanEstandar.reconstruir(modelo, semestre, Arrays.copyOf(bloque, k + 1), asignacion);
    List<String> errores = new VerificadorDePlan(modelo).verificar(plan, a0, libreInicial, tope);
    if (!errores.isEmpty()) {}
    if (!errores.isEmpty()) {}
    if (!errores.isEmpty()) {}
    return errores.isEmpty() ? plan : null;
  }

  private static boolean cabe(
      ModeloDeCalculo modelo, int materia, int creditos, int materias, Tope tope) {
    return creditos + modelo.creditos(materia) <= tope.limiteCreditos()
        && materias + 1 <= tope.limiteMaterias();
  }

  private static int bloquePermitido(
      Restricciones restricciones,
      int s,
      int L,
      int creditos,
      int materias,
      Tope tope,
      int libreInicial) {
    if (restricciones.bloqueFijo != null && restricciones.bloqueFijo[s] != null) {
      int fijo = restricciones.bloqueFijo[s];
      if (fijo > L
          || creditos + fijo > tope.limiteCreditos()
          || materias + ceilDiv(fijo, 3) > tope.limiteMaterias()) {
        return -1;
      }
      return fijo;
    }
    int maximo = Math.min(L, tope.limiteCreditos() - creditos);
    for (int b = maximo; b >= 0; b--) {
      if (libreInicial >= 3) {
        if (b > 0 && b < 3) {
          continue;
        }
      } else if (b > 0 && b != libreInicial) {
        continue;
      }
      if (materias + ceilDiv(b, 3) <= tope.limiteMaterias()) {
        return b;
      }
    }
    return -1;
  }

  private static long[] clausulasUtiles(
      ModeloDeCalculo modelo, ConjuntoDeMaterias D, Restricciones restricciones) {
    long[] utiles = new long[ConjuntoDeMaterias.palabrasNecesarias(modelo.cantidadMaterias())];
    long[] requeridas = modelo.requeridas().palabras();
    for (int i = 0; i < modelo.cantidadMaterias(); i++) {
      boolean esRequerida =
          (requeridas[i >>> 6] & (1L << (i & 63))) != 0
              || restricciones.obligatoriasExtra.contiene(i);
      if (!esRequerida || D.contiene(i)) {
        continue;
      }
      for (long[] clausula : modelo.prerrequisitos(i)) {
        if (!D.contieneAlguna(clausula)) {
          for (int j = 0; j < utiles.length; j++) {
            utiles[j] |= clausula[j];
          }
        }
      }
    }
    return utiles;
  }

  private static boolean todasRequeridas(
      ConjuntoDeMaterias D, ModeloDeCalculo modelo, Restricciones restricciones) {
    long[] requeridas = modelo.requeridas().palabras();
    for (int i = 0; i < modelo.cantidadMaterias(); i++) {
      if ((requeridas[i >>> 6] & (1L << (i & 63))) != 0 && !D.contiene(i)) {
        return false;
      }
      if (restricciones.obligatoriasExtra.contiene(i) && !D.contiene(i)) {
        return false;
      }
    }
    return true;
  }

  private static boolean esRequerida(
      int materia, ModeloDeCalculo modelo, Restricciones restricciones) {
    return modelo.requeridas().contiene(materia)
        || restricciones.obligatoriasExtra.contiene(materia);
  }

  private static boolean sigueSiendoUtil(
      ModeloDeCalculo modelo,
      int materia,
      ConjuntoDeMaterias D,
      int[] asignacion,
      Restricciones restricciones) {
    for (int grupo : modelo.gruposDeMateria(materia)) {
      if (modelo.faltan(grupo, D, asignacion, restricciones.obligatoriasExtra) > 0) {
        return true;
      }
    }
    long[] requeridas = modelo.requeridas().palabras();
    for (int i = 0; i < modelo.cantidadMaterias(); i++) {
      boolean esRequerida =
          (requeridas[i >>> 6] & (1L << (i & 63))) != 0
              || restricciones.obligatoriasExtra.contiene(i);
      if (!esRequerida || D.contiene(i)) {
        continue;
      }
      for (long[] clausula : modelo.prerrequisitos(i)) {
        if (!D.contieneAlguna(clausula)
            && (clausula[materia >>> 6] & (1L << (materia & 63))) != 0) {
          return true;
        }
      }
    }
    return false;
  }

  private static void redistribuirBloques(
      ModeloDeCalculo modelo,
      int[] semestre,
      int[] creditosPorSemestre,
      int[] bloque,
      Restricciones restricciones,
      Tope tope,
      int k,
      int libreInicial) {
    boolean cambio = true;
    while (cambio) {
      cambio = false;
      for (int receptor = 1; receptor <= k; receptor++) {
        if (creditosPorSemestre[receptor] >= modelo.minimoPorPeriodo()
            || (restricciones.bloqueFijo != null && restricciones.bloqueFijo[receptor] != null)) {
          continue;
        }
        int faltan = modelo.minimoPorPeriodo() - creditosPorSemestre[receptor];
        int donante =
            buscarDonante(
                modelo,
                semestre,
                creditosPorSemestre,
                bloque,
                restricciones,
                tope,
                k,
                receptor,
                faltan);
        if (donante < 0) {
          continue;
        }
        int movimiento = faltan;
        if (bloque[receptor] == 0 && movimiento < 3) {
          movimiento = 3;
        }
        if (bloque[donante] - movimiento < 3) {
          movimiento = bloque[donante] - 3;
        }
        if (movimiento <= 0) {
          continue;
        }
        int materiasReceptor =
            materiasDelSemestre(semestre, receptor) + ceilDiv(bloque[receptor] + movimiento, 3);
        if (materiasReceptor > tope.limiteMaterias()
            || creditosPorSemestre[receptor] + movimiento > tope.limiteCreditos()) {
          continue;
        }
        bloque[receptor] += movimiento;
        bloque[donante] -= movimiento;
        creditosPorSemestre[receptor] += movimiento;
        creditosPorSemestre[donante] -= movimiento;
        cambio = true;
        break;
      }
    }
  }

  private static int buscarDonante(
      ModeloDeCalculo modelo,
      int[] semestre,
      int[] creditosPorSemestre,
      int[] bloque,
      Restricciones restricciones,
      Tope tope,
      int k,
      int receptor,
      int faltan) {
    for (int donante = 1; donante <= k; donante++) {
      if (donante == receptor
          || (restricciones.bloqueFijo != null && restricciones.bloqueFijo[donante] != null)) {
        continue;
      }
      if (bloque[donante] < 3) {
        continue;
      }
      int movimiento = faltan;
      if (bloque[receptor] == 0 && movimiento < 3) {
        movimiento = 3;
      }
      if (bloque[donante] - movimiento < 3) {
        movimiento = bloque[donante] - 3;
      }
      if (movimiento <= 0) {
        continue;
      }
      if (creditosPorSemestre[donante] - movimiento < modelo.minimoPorPeriodo()) {
        continue;
      }
      int materiasReceptor =
          materiasDelSemestre(semestre, receptor) + ceilDiv(bloque[receptor] + movimiento, 3);
      if (materiasReceptor > tope.limiteMaterias()
          || creditosPorSemestre[receptor] + movimiento > tope.limiteCreditos()) {
        continue;
      }
      return donante;
    }
    return -1;
  }

  private static int materiasDelSemestre(int[] semestre, int s) {
    int total = 0;
    for (int valor : semestre) {
      if (valor == s) {
        total++;
      }
    }
    return total;
  }

  private static int ceilDiv(int numerador, int denominador) {
    return (numerador + denominador - 1) / denominador;
  }
}
