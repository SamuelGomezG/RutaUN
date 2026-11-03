package co.edu.unal.rutaun.normativa.aplicacion;

import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.compartido.Creditos;
import co.edu.unal.rutaun.normativa.dominio.Materia;
import co.edu.unal.rutaun.normativa.persistencia.MateriaEntidad;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;

/**
 * Convierte entre la raíz {@link Materia} y su fila {@link MateriaEntidad}. No guarda estado.
 *
 * <p>TT-04-T3 (#91). Reconstruye con {@link Materia#reconstituir}, así que no emite eventos y pasa
 * por las mismas validaciones que una materia creada (por ejemplo, créditos no negativos). Si la
 * base trae un dato inválido, sube la {@link IllegalArgumentException} del dominio.
 */
public final class MateriaMapper {

  private MateriaMapper() {}

  /** Reconstruye la materia que guarda esta fila. */
  public static Materia aDominio(MateriaEntidad entidad) {
    ArgumentChecks.obligatorio(entidad, "entidad");
    return Materia.reconstituir(
        new CodigoMateria(entidad.getCodigo()), new Creditos(entidad.getCreditos()));
  }

  /** Convierte la materia en la fila que se guarda en la base. */
  public static MateriaEntidad aEntidad(Materia materia) {
    ArgumentChecks.obligatorio(materia, "materia");
    return new MateriaEntidad(materia.codigo().valor(), materia.creditos().valor());
  }
}
