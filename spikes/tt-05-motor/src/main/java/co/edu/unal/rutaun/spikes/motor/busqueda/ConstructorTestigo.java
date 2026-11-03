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
    ConjuntoDeMaterias cursadas = a0.copia();
    int[] asignacion = modelo.asignacionInicial();
    for (int i = 0; i < n; i++) {
      if ((cursadas.contiene(i) || restricciones.obligatoriasExtra.contiene(i))
          && asignacion[i] < 0) {
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
            Math.min(
                ModeloDeCalculo.LIMITE_DE_SEMESTRES, modelo.cantidadMaterias() + libreInicial + 1));
    if (maximoSemestres < 1) {
      return null;
    }
    int[] bloque = new int[maximoSemestres + 1];
    int[] creditosPorSemestre = new int[maximoSemestres + 1];
    int libreRestante = libreInicial;
    int marcados = 0;
    int creditosOptativas = 0;
    int k = 0;

    for (int s = 1; s <= maximoSemestres; s++) {
      if (cancelacion.cancelado()) {
        return null;
      }
      if (libreRestante == 0
          && modelo.todoCubierto(cursadas, asignacion)
          && todasRequeridas(cursadas, modelo, restricciones)) {
        k = s - 1;
        break;
      }
      for (int i = 0; i < n; i++) {
        if (restricciones.semestreFijo[i] >= 0
            && restricciones.semestreFijo[i] < s
            && !cursadas.contiene(i)) {
          return null;
        }
      }

      long[] utiles = Seleccion.clausulasUtiles(modelo, cursadas, restricciones);
      List<Integer> requeridos = new ArrayList<>();
      List<Integer> optativas = new ArrayList<>();
      for (int i = 0; i < n; i++) {
        if (cancelacion.cancelado()) {
          return null;
        }
        if (cursadas.contiene(i) || !modelo.cursable(i) || restricciones.excluidas.contiene(i)) {
          continue;
        }
        if (restricciones.semestreFijo[i] >= 0 && restricciones.semestreFijo[i] != s) {
          continue;
        }
        if (restricciones.semestreMaximo[i] < s) {
          continue;
        }
        if (!modelo.clausulasCumplidas(modelo.prerrequisitos(i), cursadas)) {
          continue;
        }
        if (!modelo.habilitacionesCumplidas(
            i, cursadas, libreInicial - libreRestante, asignacion)) {
          continue;
        }
        if (Seleccion.esRequerida(i, modelo, restricciones)) {
          requeridos.add(i);
        } else {
          if (restricciones.soloIncluidas && !restricciones.incluidas.contiene(i)) {
            continue;
          }
          boolean porAgrupacion = false;
          for (int grupo : modelo.gruposDeMateria(i)) {
            if (modelo.faltan(grupo, cursadas, asignacion, restricciones.obligatoriasExtra) > 0) {
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
      ConjuntoDeMaterias seleccionadas = cursadas.copia();
      int[] asignacionSeleccion = asignacion.clone();
      for (int materia : requeridos) {
        if (restricciones.semestreFijo[materia] == s
            && !cabe(modelo, materia, creditos, materias, tope)) {
          return null;
        }
        if (cabe(modelo, materia, creditos, materias, tope)) {
          seleccion.add(materia);
          creditos += modelo.creditos(materia);
          materias++;
          Seleccion.elegirAgrupacion(
              modelo, materia, seleccionadas, asignacionSeleccion, restricciones);
          seleccionadas.agregar(materia);
        }
      }
      for (int materia : optativas) {
        if (cancelacion.cancelado()) {
          return null;
        }
        if (!cabe(modelo, materia, creditos, materias, tope)) {
          continue;
        }
        if (!sigueSiendoUtil(modelo, materia, seleccionadas, asignacionSeleccion, restricciones)) {
          continue;
        }
        seleccion.add(materia);
        creditos += modelo.creditos(materia);
        materias++;
        Seleccion.elegirAgrupacion(
            modelo, materia, seleccionadas, asignacionSeleccion, restricciones);
        seleccionadas.agregar(materia);
      }

      // Cada materia entra como pareja a lo sumo una vez por semestre y, al quitarla, queda
      // descartada y no vuelve a proponerse; el bucle da como mucho 2·n vueltas.
      boolean[] descartadas = new boolean[n];
      boolean cambio = true;
      while (cambio) {
        if (cancelacion.cancelado()) {
          return null;
        }
        cambio = false;
        for (int indice = 0; indice < seleccion.size(); indice++) {
          int materia = seleccion.get(indice);
          if (modelo.clausulasCumplidas(modelo.correquisitos(materia), seleccionadas)) {
            continue;
          }
          int par =
              buscarCorrequisito(
                  modelo,
                  materia,
                  cursadas,
                  seleccionadas,
                  asignacionSeleccion,
                  restricciones,
                  tope,
                  creditos,
                  materias,
                  libreInicial - libreRestante,
                  s,
                  descartadas,
                  cancelacion);
          if (par >= 0) {
            seleccion.add(par);
            creditos += modelo.creditos(par);
            materias++;
            Seleccion.elegirAgrupacion(
                modelo, par, seleccionadas, asignacionSeleccion, restricciones);
            seleccionadas.agregar(par);
          } else {
            seleccion.remove(indice);
            descartadas[materia] = true;
            creditos -= modelo.creditos(materia);
            materias--;
            seleccionadas.quitar(materia);
            asignacionSeleccion[materia] = -1;
          }
          cambio = true;
          break;
        }
      }
      cursadas = seleccionadas;
      asignacion = asignacionSeleccion;
      for (int materia : seleccion) {
        semestre[materia] = s;
        if (!modelo.requeridas().contiene(materia)) {
          creditosOptativas += modelo.creditos(materia);
        }
      }
      for (int materia : requeridos) {
        if (!cursadas.contiene(materia) && restricciones.semestreFijo[materia] == s) {
          return null;
        }
      }
      for (int i = 0; i < n; i++) {
        if (restricciones.semestreMaximo[i] == s && !cursadas.contiene(i)) {
          return null;
        }
      }

      int b =
          bloquePermitido(
              modelo, restricciones, s, libreRestante, creditos, materias, tope, libreInicial);
      if (b < 0) {
        return null;
      }
      libreRestante -= b;
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
    if (libreRestante != 0
        || !modelo.todoCubierto(cursadas, asignacion)
        || !todasRequeridas(cursadas, modelo, restricciones)) {
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
    return errores.isEmpty() ? plan : null;
  }

  private static boolean cabe(
      ModeloDeCalculo modelo, int materia, int creditos, int materias, Tope tope) {
    return creditos + modelo.creditos(materia) <= tope.limiteCreditos()
        && materias + 1 <= tope.limiteMaterias();
  }

  private static int bloquePermitido(
      ModeloDeCalculo modelo,
      Restricciones restricciones,
      int s,
      int libreRestante,
      int creditos,
      int materias,
      Tope tope,
      int libreInicial) {
    if (restricciones.bloqueFijo != null && restricciones.bloqueFijo[s] != null) {
      int fijo = restricciones.bloqueFijo[s];
      if (fijo > libreRestante
          || creditos + fijo > tope.limiteCreditos()
          || materias + Math.ceilDiv(fijo, modelo.creditosPorMateriaLibre())
              > tope.limiteMaterias()) {
        return -1;
      }
      return fijo;
    }
    int maximo = Math.min(libreRestante, tope.limiteCreditos() - creditos);
    for (int b = maximo; b >= 0; b--) {
      int creditosPorMateria = modelo.creditosPorMateriaLibre();
      if (libreInicial >= creditosPorMateria) {
        if (b > 0 && b < creditosPorMateria) {
          continue;
        }
      } else if (b > 0 && b != libreInicial) {
        continue;
      }
      if (materias + Math.ceilDiv(b, modelo.creditosPorMateriaLibre()) <= tope.limiteMaterias()) {
        return b;
      }
    }
    return -1;
  }

  private static boolean todasRequeridas(
      ConjuntoDeMaterias cursadas, ModeloDeCalculo modelo, Restricciones restricciones) {
    long[] requeridas = modelo.requeridas().palabras();
    for (int i = 0; i < modelo.cantidadMaterias(); i++) {
      if ((requeridas[i >>> 6] & (1L << (i & 63))) != 0 && !cursadas.contiene(i)) {
        return false;
      }
      if (restricciones.obligatoriasExtra.contiene(i) && !cursadas.contiene(i)) {
        return false;
      }
    }
    return true;
  }

  private static int buscarCorrequisito(
      ModeloDeCalculo modelo,
      int materia,
      ConjuntoDeMaterias previas,
      ConjuntoDeMaterias seleccionadas,
      int[] asignacion,
      Restricciones restricciones,
      Tope tope,
      int creditos,
      int materias,
      int libreColocado,
      int semestre,
      boolean[] descartadas,
      Cancelacion cancelacion) {
    for (long[] clausula : modelo.correquisitos(materia)) {
      if (seleccionadas.contieneAlguna(clausula)) {
        continue;
      }
      for (int i = 0; i < modelo.cantidadMaterias(); i++) {
        if (cancelacion.cancelado()) {
          return -1;
        }
        if ((clausula[i >>> 6] & (1L << (i & 63))) == 0
            || seleccionadas.contiene(i)
            || descartadas[i]
            || !modelo.cursable(i)
            || restricciones.excluidas.contiene(i)) {
          continue;
        }
        if (restricciones.semestreFijo[i] >= 0 && restricciones.semestreFijo[i] != semestre) {
          continue;
        }
        if (restricciones.semestreMaximo[i] < semestre) {
          continue;
        }
        if (!Seleccion.esRequerida(i, modelo, restricciones)
            && restricciones.soloIncluidas
            && !restricciones.incluidas.contiene(i)) {
          continue;
        }
        if (!modelo.clausulasCumplidas(modelo.prerrequisitos(i), previas)) {
          continue;
        }
        if (!modelo.habilitacionesCumplidas(i, previas, libreColocado, asignacion)) {
          continue;
        }
        if (!cabe(modelo, i, creditos, materias, tope)) {
          continue;
        }
        return i;
      }
      return -1;
    }
    return -1;
  }

  private static boolean sigueSiendoUtil(
      ModeloDeCalculo modelo,
      int materia,
      ConjuntoDeMaterias cursadas,
      int[] asignacion,
      Restricciones restricciones) {
    for (int grupo : modelo.gruposDeMateria(materia)) {
      if (modelo.faltan(grupo, cursadas, asignacion, restricciones.obligatoriasExtra) > 0) {
        return true;
      }
    }
    long[] requeridas = modelo.requeridas().palabras();
    for (int i = 0; i < modelo.cantidadMaterias(); i++) {
      boolean esRequerida =
          (requeridas[i >>> 6] & (1L << (i & 63))) != 0
              || restricciones.obligatoriasExtra.contiene(i);
      if (!esRequerida || cursadas.contiene(i)) {
        continue;
      }
      for (long[] clausula : modelo.prerrequisitos(i)) {
        if (!cursadas.contieneAlguna(clausula)
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
        if (bloque[receptor] == 0 && movimiento < modelo.creditosPorMateriaLibre()) {
          movimiento = 3;
        }
        if (bloque[donante] - movimiento < modelo.creditosPorMateriaLibre()) {
          movimiento = bloque[donante] - 3;
        }
        if (movimiento <= 0) {
          continue;
        }
        int materiasReceptor =
            materiasDelSemestre(semestre, receptor)
                + Math.ceilDiv(bloque[receptor] + movimiento, modelo.creditosPorMateriaLibre());
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
      if (bloque[donante] < modelo.creditosPorMateriaLibre()) {
        continue;
      }
      int movimiento = faltan;
      if (bloque[receptor] == 0 && movimiento < modelo.creditosPorMateriaLibre()) {
        movimiento = 3;
      }
      if (bloque[donante] - movimiento < modelo.creditosPorMateriaLibre()) {
        movimiento = bloque[donante] - 3;
      }
      if (movimiento <= 0) {
        continue;
      }
      if (creditosPorSemestre[donante] - movimiento < modelo.minimoPorPeriodo()) {
        continue;
      }
      int materiasReceptor =
          materiasDelSemestre(semestre, receptor)
              + Math.ceilDiv(bloque[receptor] + movimiento, modelo.creditosPorMateriaLibre());
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
}
