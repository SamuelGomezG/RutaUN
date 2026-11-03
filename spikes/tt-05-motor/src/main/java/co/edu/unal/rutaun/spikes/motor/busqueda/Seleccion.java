package co.edu.unal.rutaun.spikes.motor.busqueda;

import co.edu.unal.rutaun.spikes.motor.modelo.ConjuntoDeMaterias;
import co.edu.unal.rutaun.spikes.motor.modelo.ModeloDeCalculo;

/** Reglas de selección compartidas por el buscador y el constructor testigo. */
final class Seleccion {

  private Seleccion() {}

  static long[] clausulasUtiles(
      ModeloDeCalculo modelo, ConjuntoDeMaterias cursadas, Restricciones restricciones) {
    long[] utiles = new long[ConjuntoDeMaterias.palabrasNecesarias(modelo.cantidadMaterias())];
    for (int i = 0; i < modelo.cantidadMaterias(); i++) {
      if (!esRequerida(i, modelo, restricciones) || cursadas.contiene(i)) {
        continue;
      }
      for (long[] clausula : modelo.prerrequisitos(i)) {
        if (!cursadas.contieneAlguna(clausula)) {
          for (int j = 0; j < utiles.length; j++) {
            utiles[j] |= clausula[j];
          }
        }
      }
    }
    return utiles;
  }

  static boolean esRequerida(int materia, ModeloDeCalculo modelo, Restricciones restricciones) {
    return modelo.requeridas().contiene(materia)
        || restricciones.obligatoriasExtra.contiene(materia);
  }

  static void elegirAgrupacion(
      ModeloDeCalculo modelo,
      int materia,
      ConjuntoDeMaterias cursadas,
      int[] asignacion,
      Restricciones restricciones) {
    int[] grupos = modelo.gruposDeMateria(materia);
    if (asignacion[materia] >= 0 || grupos.length == 0) {
      return;
    }
    int mejor = grupos[0];
    int mejorFaltan = -1;
    for (int grupo : grupos) {
      int faltan = modelo.faltan(grupo, cursadas, asignacion, restricciones.obligatoriasExtra);
      if (faltan > mejorFaltan) {
        mejorFaltan = faltan;
        mejor = grupo;
      }
    }
    asignacion[materia] = mejor;
  }
}
