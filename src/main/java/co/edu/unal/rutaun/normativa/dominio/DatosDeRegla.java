package co.edu.unal.rutaun.normativa.dominio;

import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.compartido.Creditos;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.util.Set;

/**
 * Datos propios de una regla según su categoría (glosario, «Datos de una regla»). La categoría de
 * la regla es el tipo de sus datos: un {@code switch} exhaustivo sobre esta interfaz cubre todas
 * las categorías y el compilador avisa cuando se agrega una nueva.
 */
public sealed interface DatosDeRegla {

  /**
   * Datos de una regla de materia: la materia en la malla y el componente o la agrupación que la
   * contiene.
   *
   * @param materia materia con sus datos en la malla
   * @param contenedor componente o agrupación que la contiene
   */
  record DeMateria(MateriaEnMalla materia, NodoDeMalla contenedor) implements DatosDeRegla {

    /** Exige la materia y el contenedor. */
    public DeMateria {
      ArgumentChecks.obligatorio(materia, "materia");
      ArgumentChecks.obligatorio(contenedor, "contenedor");
    }
  }

  /**
   * Datos de una regla de prerrequisito.
   *
   * @param prerrequisito restricción de prerrequisito
   */
  record DePrerrequisito(Prerrequisito prerrequisito) implements DatosDeRegla {

    /** Exige el prerrequisito. */
    public DePrerrequisito {
      ArgumentChecks.obligatorio(prerrequisito, "prerrequisito");
    }
  }

  /**
   * Datos de una regla de correquisito.
   *
   * @param correquisito restricción de correquisito
   */
  record DeCorrequisito(Correquisito correquisito) implements DatosDeRegla {

    /** Exige el correquisito. */
    public DeCorrequisito {
      ArgumentChecks.obligatorio(correquisito, "correquisito");
    }
  }

  /**
   * Datos de una regla de habilitación.
   *
   * @param regla restricción de habilitación
   */
  record DeHabilitacion(ReglaDeHabilitacion regla) implements DatosDeRegla {

    /** Exige la regla de habilitación. */
    public DeHabilitacion {
      ArgumentChecks.obligatorio(regla, "regla");
    }
  }

  /**
   * Datos de una regla de agrupación: la agrupación y el componente o la agrupación que la
   * contiene.
   *
   * @param agrupacion agrupación con sus datos
   * @param contenedor componente o agrupación que la contiene
   */
  record DeAgrupacion(Agrupacion agrupacion, NodoDeMalla contenedor) implements DatosDeRegla {

    /** Exige la agrupación y el contenedor. */
    public DeAgrupacion {
      ArgumentChecks.obligatorio(agrupacion, "agrupacion");
      ArgumentChecks.obligatorio(contenedor, "contenedor");
    }
  }

  /**
   * Datos de una regla de componente, incluido el de libre elección.
   *
   * @param componente componente al que se refiere la regla
   */
  record DeComponente(NodoDeMalla componente) implements DatosDeRegla {

    /** Exige un componente, no una agrupación. */
    public DeComponente {
      ArgumentChecks.obligatorio(componente, "componente");
      if (!(componente instanceof Componente)
          && !(componente instanceof ComponenteDeLibreEleccion)) {
        throw new IllegalArgumentException("componente debe ser un componente de la malla");
      }
    }
  }

  /**
   * Datos de una regla del programa: sus créditos exigidos.
   *
   * @param creditosExigidos créditos exigidos del plan de estudios
   */
  record DePrograma(Creditos creditosExigidos) implements DatosDeRegla {

    /** Exige los créditos. */
    public DePrograma {
      ArgumentChecks.obligatorio(creditosExigidos, "creditosExigidos");
    }
  }

  /**
   * Datos de una regla de equivalencia: las materias de origen y la materia de destino.
   *
   * @param origen materias que se entienden aprobadas
   * @param destino materia que se reconoce
   */
  record DeEquivalencia(Set<CodigoMateria> origen, CodigoMateria destino) implements DatosDeRegla {

    /** Exige al menos una materia de origen y la de destino; deja el conjunto inmutable. */
    public DeEquivalencia {
      origen = ArgumentChecks.copiaSinNulosDeConjunto(origen, "origen");
      if (origen.isEmpty()) {
        throw new IllegalArgumentException("origen no puede ser vacío");
      }
      ArgumentChecks.obligatorio(destino, "destino");
    }
  }

  /**
   * Datos de una regla de carga mínima o máxima por periodo.
   *
   * @param creditos créditos de la carga
   * @param tipo si la carga es mínima o máxima
   */
  record DeCargaPorPeriodo(Creditos creditos, TipoDeCargaPorPeriodo tipo) implements DatosDeRegla {

    /** Exige los créditos y el tipo. */
    public DeCargaPorPeriodo {
      ArgumentChecks.obligatorio(creditos, "creditos");
      ArgumentChecks.obligatorio(tipo, "tipo");
    }
  }

  /** Datos de una regla de requisitos de grado: no lleva datos adicionales. */
  record DeRequisitoDeGrado() implements DatosDeRegla {}

  /** Datos de una regla de la categoría «otra»: no lleva datos adicionales. */
  record DeOtraCategoria() implements DatosDeRegla {}
}
