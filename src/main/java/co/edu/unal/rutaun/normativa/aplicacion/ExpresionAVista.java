package co.edu.unal.rutaun.normativa.aplicacion;

import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.normativa.ExpresionVista;
import co.edu.unal.rutaun.normativa.dominio.ExpresionDeMaterias;
import co.edu.unal.rutaun.normativa.dominio.TipoMateriaExterna;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * Convierte la expresión de un prerrequisito o un correquisito en la vista que muestra UC05 (paso
 * 4). Junta los términos del mismo tipo («A y (B y C)» es «A, B y C») y no muestra las materias
 * externas (ext. 4k): un término externo no aparece, y si ninguno queda, la restricción no tiene
 * qué mostrar y devuelve vacío.
 *
 * <p>Simplificación de UC05-HU01: toda materia externa se omite igual, sea de nivelación o de otro
 * programa. Qué hacer cuando la expresión no se puede cumplir sin una materia de otro programa lo
 * dice {@link #noSePuedeCumplir}; la parte de UC05-HU07 (#33) que falta es la del avance del
 * estudiante.
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
      case ExpresionDeMaterias.TerminoMateria materia ->
          Optional.of(
              new ExpresionVista.Materia(materia.codigo(), nombres.apply(materia.codigo())));
      case ExpresionDeMaterias.MateriaExterna externa -> Optional.empty();
      case ExpresionDeMaterias.TodasLas todas -> combinar(true, todas.terminos(), nombres);
      case ExpresionDeMaterias.AlgunaDe alguna -> combinar(false, alguna.terminos(), nombres);
    };
  }

  /**
   * Si la expresión no se puede cumplir desde el programa (UC05 ext. 4k): una materia de otro
   * programa no se puede cumplir; una de nivelación se da por cumplida; una conjunción no se puede
   * cumplir si alguno de sus términos no se puede, y una disyunción solo si ninguno puede.
   *
   * @param expresion expresión de un prerrequisito
   * @return {@code true} si la materia que la exige no se puede cursar sin una materia de otro
   *     programa
   */
  static boolean noSePuedeCumplir(ExpresionDeMaterias expresion) {
    ArgumentChecks.obligatorio(expresion, "expresion");
    return switch (expresion) {
      case ExpresionDeMaterias.TerminoMateria materia -> false;
      case ExpresionDeMaterias.MateriaExterna externa ->
          externa.tipo() == TipoMateriaExterna.OTRO_PROGRAMA;
      case ExpresionDeMaterias.TodasLas todas ->
          todas.terminos().stream().anyMatch(ExpresionAVista::noSePuedeCumplir);
      case ExpresionDeMaterias.AlgunaDe alguna ->
          alguna.terminos().stream().allMatch(ExpresionAVista::noSePuedeCumplir);
    };
  }

  /** Junta los términos con «y» o con «o», omitiendo los que no tienen vista. */
  private static Optional<ExpresionVista> combinar(
      boolean conjuncion,
      Set<ExpresionDeMaterias> terminos,
      Function<CodigoMateria, String> nombres) {
    List<ExpresionVista> partes = new ArrayList<>();
    for (ExpresionDeMaterias termino : terminos) {
      aVista(termino, nombres).ifPresent(vista -> agregar(conjuncion, vista, partes));
    }
    if (partes.isEmpty()) {
      return Optional.empty();
    }
    if (partes.size() == 1) {
      return Optional.of(partes.get(0));
    }
    return Optional.of(
        conjuncion ? new ExpresionVista.TodasLas(partes) : new ExpresionVista.AlgunaDe(partes));
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
