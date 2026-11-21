package co.edu.unal.rutaun.normativa.aplicacion;

import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.normativa.ProgramaVista;
import co.edu.unal.rutaun.normativa.RestriccionesDeMateriaVista;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;

/**
 * Resultado de pedir las restricciones de una materia (UC05 paso 4): lo que exige, o la razón por
 * la que no hay nada que explicar. Sellado, para que quien lo recibe trate todos los casos.
 */
public sealed interface RestriccionesDeLaMateria {

  /**
   * El programa y lo que la materia exige en su consolidado vigente. La materia puede no exigir
   * nada (UC05 ext. 3a), y una restricción puede no tener fuente (ext. 4d); en ese caso el
   * incidente ya se publicó.
   *
   * @param programa el programa consultado
   * @param restricciones lo que la materia exige, con la versión del consolidado
   */
  record Explicadas(ProgramaVista programa, RestriccionesDeMateriaVista restricciones)
      implements RestriccionesDeLaMateria {

    /** Exige los dos datos. */
    public Explicadas {
      ArgumentChecks.obligatorio(programa, "programa");
      ArgumentChecks.obligatorio(restricciones, "restricciones");
    }
  }

  /** El programa no está cargado (UC05 precondición 1). */
  record ProgramaNoCargado() implements RestriccionesDeLaMateria {}

  /**
   * El programa está cargado pero no tiene una versión vigente del consolidado (UC05 precondición
   * 1).
   *
   * @param programa el programa consultado
   */
  record SinVersionVigente(ProgramaVista programa) implements RestriccionesDeLaMateria {

    /** Exige el programa. */
    public SinVersionVigente {
      ArgumentChecks.obligatorio(programa, "programa");
    }
  }

  /**
   * La materia no figura en el consolidado vigente del programa.
   *
   * @param programa el programa consultado
   * @param materia código de la materia pedida
   */
  record MateriaNoEncontrada(ProgramaVista programa, CodigoMateria materia)
      implements RestriccionesDeLaMateria {

    /** Exige los dos datos. */
    public MateriaNoEncontrada {
      ArgumentChecks.obligatorio(programa, "programa");
      ArgumentChecks.obligatorio(materia, "materia");
    }
  }
}
