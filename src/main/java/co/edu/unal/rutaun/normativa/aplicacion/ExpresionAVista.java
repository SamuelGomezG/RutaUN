package co.edu.unal.rutaun.normativa.aplicacion;

import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.normativa.ExpresionVista;
import co.edu.unal.rutaun.normativa.dominio.ExpresionDeMaterias;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Convierte la expresión de un prerrequisito o un correquisito en la vista que muestra UC05 (paso
 * 4). Junta los términos del mismo tipo («A y (B y C)» es «A, B y C») y no muestra las materias
 * externas (ext. 4k): un término externo no aparece, y si ninguno queda, la restricción no tiene
 * qué mostrar y devuelve vacío.
 *
 * <p>Simplificación de UC05-HU01: toda materia externa se omite igual, sea de nivelación o de otro
 * programa. La señal «no se puede cursar desde este programa» y el trato distinto de cada origen
 * son de UC05-HU07 (#33).
 */
final class ExpresionAVista {

  private ExpresionAVista() {}

  /**
   * La vista de la expresión, o vacío si solo menciona materias externas.
   *
   * @param expresion expresión de la regla
   * @param nombres nombre de una materia en la versión vigente; recibe su código
   */
  static Optional<ExpresionVista> aVista(
      ExpresionDeMaterias expresion, Function<CodigoMateria, String> nombres) {
    ArgumentChecks.obligatorio(expresion, "expresion");
    ArgumentChecks.obligatorio(nombres, "nombres");
    return switch (expresion) {
      case ExpresionDeMaterias.MateriaDelPrograma materia ->
          Optional.of(
              new ExpresionVista.Materia(materia.codigo(), nombres.apply(materia.codigo())));
      case ExpresionDeMaterias.MateriaExterna externa -> Optional.empty();
      case ExpresionDeMaterias.Y y ->
          combinar(true, aVista(y.izquierda(), nombres), aVista(y.derecha(), nombres));
      case ExpresionDeMaterias.O o ->
          combinar(false, aVista(o.izquierda(), nombres), aVista(o.derecha(), nombres));
    };
  }

  /** Junta dos lados con «y» o con «o»; si falta uno, queda el otro. */
  private static Optional<ExpresionVista> combinar(
      boolean conjuncion, Optional<ExpresionVista> izquierda, Optional<ExpresionVista> derecha) {
    if (izquierda.isEmpty() || derecha.isEmpty()) {
      return izquierda.or(() -> derecha);
    }
    List<ExpresionVista> terminos = new ArrayList<>();
    agregar(conjuncion, izquierda.get(), terminos);
    agregar(conjuncion, derecha.get(), terminos);
    return Optional.of(
        conjuncion ? new ExpresionVista.TodasLas(terminos) : new ExpresionVista.AlgunaDe(terminos));
  }

  /** Agrega el término; si es del mismo tipo que la combinación, agrega los suyos. */
  private static void agregar(
      boolean conjuncion, ExpresionVista termino, List<ExpresionVista> terminos) {
    switch (termino) {
      case ExpresionVista.TodasLas todas when conjuncion -> terminos.addAll(todas.terminos());
      case ExpresionVista.AlgunaDe alguna when !conjuncion ->
          terminos.addAll(alguna.alternativas());
      default -> terminos.add(termino);
    }
  }
}
