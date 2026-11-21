package co.edu.unal.rutaun.normativa;

import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.compartido.ProgramaId;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.time.LocalDate;
import java.util.List;

/**
 * Vista inmutable de lo que una materia exige para cursarla en el consolidado vigente de un
 * programa (UC05 pasos 2 a 4).
 *
 * @param programa programa consultado
 * @param version número de la versión vigente del consolidado con que se explicó, la misma con que
 *     UC06 muestra las reglas
 * @param fecha fecha de esa versión
 * @param materia código de la materia
 * @param nombre nombre de la materia en esa versión
 * @param restricciones sus prerrequisitos y correquisitos: primero los prerrequisitos y, dentro de
 *     cada clase, por fecha de expedición de su norma, artículo e identificador; el mismo dato
 *     produce siempre la misma vista. Vacía si la materia no exige nada (ext. 3a)
 * @param noCursable si el prerrequisito de la materia solo se cumple con una materia de otro
 *     programa, así que no se puede cursar desde el programa (ext. 4k)
 */
public record RestriccionesDeMateriaVista(
    ProgramaId programa,
    int version,
    LocalDate fecha,
    CodigoMateria materia,
    String nombre,
    List<RestriccionVista> restricciones,
    boolean noCursable) {

  /** Una materia que sí se puede cursar desde el programa. */
  public RestriccionesDeMateriaVista(
      ProgramaId programa,
      int version,
      LocalDate fecha,
      CodigoMateria materia,
      String nombre,
      List<RestriccionVista> restricciones) {
    this(programa, version, fecha, materia, nombre, restricciones, false);
  }

  /** Exige todos los datos y deja la lista inmutable. */
  public RestriccionesDeMateriaVista {
    ArgumentChecks.obligatorio(programa, "programa");
    ArgumentChecks.obligatorioPositivo(version, "version");
    ArgumentChecks.obligatorio(fecha, "fecha");
    ArgumentChecks.obligatorio(materia, "materia");
    ArgumentChecks.obligatorioNoVacio(nombre, "nombre");
    restricciones = ArgumentChecks.copiaSinNulos(restricciones, "restricciones");
  }

  /**
   * Si la materia no tiene restricciones que mostrar (UC05 ext. 3a). Una materia que no se puede
   * cursar desde el programa sí tiene una, aunque no se muestre: la de la materia externa.
   */
  public boolean sinRestricciones() {
    return restricciones.isEmpty() && !noCursable;
  }
}
