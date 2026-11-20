package co.edu.unal.rutaun.normativa.dominio;

import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.compartido.Creditos;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.util.Optional;

/**
 * Una materia tal como figura en la malla de una versión del consolidado: con el nombre que tiene
 * en esa versión (el catálogo no lo guarda), su semestre sugerido, si la malla estándar lo da, y si
 * es cursable desde el programa.
 *
 * @param codigo código de la materia, que es su identidad en el catálogo
 * @param nombre nombre de la materia en la versión
 * @param creditos créditos de la materia
 * @param obligatoriedad si es obligatoria u optativa donde figura
 * @param semestreSugerido semestre sugerido en la malla estándar; vacío si no lo tiene
 * @param cursableDesdeElPrograma falso si su prerrequisito solo puede cumplirse con una materia de
 *     otro programa (glosario, «Materia no cursable desde el programa»; S17); lo calcula el
 *     compilador del consolidado, y quien lee la norma lo deja en verdadero
 */
public record MateriaEnMalla(
    CodigoMateria codigo,
    String nombre,
    Creditos creditos,
    Obligatoriedad obligatoriedad,
    Optional<Integer> semestreSugerido,
    boolean cursableDesdeElPrograma) {

  /** Exige todos los datos; el semestre sugerido, si está, es mayor que cero. */
  public MateriaEnMalla {
    ArgumentChecks.obligatorio(codigo, "codigo");
    ArgumentChecks.obligatorioNoVacio(nombre, "nombre");
    ArgumentChecks.obligatorio(creditos, "creditos");
    ArgumentChecks.obligatorio(obligatoriedad, "obligatoriedad");
    ArgumentChecks.obligatorio(semestreSugerido, "semestreSugerido")
        .ifPresent(semestre -> ArgumentChecks.obligatorioPositivo(semestre, "semestreSugerido"));
  }

  /** Materia sin semestre sugerido. */
  public static MateriaEnMalla sinSemestre(
      CodigoMateria codigo, String nombre, Creditos creditos, Obligatoriedad obligatoriedad) {
    return new MateriaEnMalla(codigo, nombre, creditos, obligatoriedad, Optional.empty(), true);
  }

  /** Materia con semestre sugerido. */
  public static MateriaEnMalla enSemestre(
      CodigoMateria codigo,
      String nombre,
      Creditos creditos,
      Obligatoriedad obligatoriedad,
      int semestre) {
    return new MateriaEnMalla(
        codigo, nombre, creditos, obligatoriedad, Optional.of(semestre), true);
  }
}
