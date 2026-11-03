package co.edu.unal.rutaun.normativa.aplicacion;

import co.edu.unal.rutaun.normativa.ConsolidadoVista;
import co.edu.unal.rutaun.normativa.ProgramaVista;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;

/**
 * Resultado de pedir las reglas de un programa (UC06 paso 4): las reglas, o la razón por la que no
 * hay nada que mostrar. Sellado, para que quien lo recibe trate todos los casos.
 */
public sealed interface ReglasDelPrograma {

  /**
   * El programa y su consolidado vigente. El consolidado puede no tener reglas (UC06 ext. 4a); en
   * ese caso el incidente ya se publicó.
   *
   * @param programa el programa consultado
   * @param consolidado su consolidado vigente
   */
  record Consultadas(ProgramaVista programa, ConsolidadoVista consolidado)
      implements ReglasDelPrograma {

    /** Exige los dos datos. */
    public Consultadas {
      ArgumentChecks.obligatorio(programa, "programa");
      ArgumentChecks.obligatorio(consolidado, "consolidado");
    }
  }

  /** El programa no está cargado. */
  record ProgramaNoCargado() implements ReglasDelPrograma {}

  /**
   * El programa está cargado pero no tiene una versión vigente del consolidado.
   *
   * @param programa el programa consultado
   */
  record SinVersionVigente(ProgramaVista programa) implements ReglasDelPrograma {

    /** Exige el programa. */
    public SinVersionVigente {
      ArgumentChecks.obligatorio(programa, "programa");
    }
  }
}
