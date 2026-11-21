package co.edu.unal.rutaun.compartido;

import co.edu.unal.rutaun.utilidades.ArgumentChecks;

/**
 * Razón de una restricción, estructurada para que la presentación la traduzca (UC05).
 *
 * <p>Ninguna variante lleva el nombre ni el código de una materia externa: RutaUN no las muestra en
 * ninguna parte (S17; glosario, «Materia externa»). Una {@link CodigoMateria} en una razón de
 * término es siempre una materia del programa.
 */
public sealed interface Razon {

  /** Una materia del programa está aprobada. */
  record TerminoAprobado(CodigoMateria materia) implements Razon {

    /** Exige la materia. */
    public TerminoAprobado {
      ArgumentChecks.obligatorio(materia, "materia");
    }
  }

  /** Una materia del programa está en curso. */
  record TerminoEnCurso(CodigoMateria materia) implements Razon {

    /** Exige la materia. */
    public TerminoEnCurso {
      ArgumentChecks.obligatorio(materia, "materia");
    }
  }

  /** Una materia del programa está pendiente. */
  record TerminoPendiente(CodigoMateria materia) implements Razon {

    /** Exige la materia. */
    public TerminoPendiente {
      ArgumentChecks.obligatorio(materia, "materia");
    }
  }

  /** Una materia del programa ya no rige y no tiene equivalencia que la traduzca. */
  record TerminoQueYaNoRige(CodigoMateria materia) implements Razon {

    /** Exige la materia. */
    public TerminoQueYaNoRige {
      ArgumentChecks.obligatorio(materia, "materia");
    }
  }

  /** El plan ubica una materia del programa en el mismo semestre o antes. */
  record UbicadoPorElPlan(CodigoMateria materia) implements Razon {

    /** Exige la materia. */
    public UbicadoPorElPlan {
      ArgumentChecks.obligatorio(materia, "materia");
    }
  }

  /**
   * Créditos aprobados y umbral de una regla de habilitación.
   *
   * @param aprobados créditos aprobados de la medida
   * @param umbral créditos que se deben aprobar
   */
  record CreditosDeHabilitacion(Creditos aprobados, Creditos umbral) implements Razon {

    /** Exige los dos datos. */
    public CreditosDeHabilitacion {
      ArgumentChecks.obligatorio(aprobados, "aprobados");
      ArgumentChecks.obligatorio(umbral, "umbral");
    }
  }

  /** La expresión no se puede cumplir sin una materia de otro programa (S17). */
  record NoCursableDesdeElPrograma() implements Razon {}
}
