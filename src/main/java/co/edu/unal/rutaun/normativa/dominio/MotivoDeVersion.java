package co.edu.unal.rutaun.normativa.dominio;

import co.edu.unal.rutaun.compartido.NormaId;
import co.edu.unal.rutaun.compartido.PeriodoAcademico;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.time.LocalDate;

/**
 * Por qué nace una versión del consolidado (diseño de clases, «Enums y tipos sellados»; glosario,
 * «Historial de versiones»). Cada motivo lleva sus propios datos, así que ningún método necesita
 * parámetros opcionales, y un {@code switch} exhaustivo cubre todos los casos.
 */
public sealed interface MotivoDeVersion {

  /**
   * La versión incorpora una norma.
   *
   * @param norma norma incorporada
   * @param administrador administrador de normativa que la incorporó
   */
  record PorIncorporacion(NormaId norma, String administrador) implements MotivoDeVersion {

    /** Exige la norma y el administrador. */
    public PorIncorporacion {
      ArgumentChecks.obligatorio(norma, "norma");
      ArgumentChecks.obligatorioNoVacio(administrador, "administrador");
    }
  }

  /**
   * La versión corrige la anterior.
   *
   * @param administrador administrador de normativa que hizo la corrección
   */
  record PorCorreccion(String administrador) implements MotivoDeVersion {

    /** Exige el administrador. */
    public PorCorreccion {
      ArgumentChecks.obligatorioNoVacio(administrador, "administrador");
    }
  }

  /**
   * La versión la crea el sistema porque una regla empezó o dejó de regir con el avance del periodo
   * en curso.
   *
   * @param periodo periodo que la originó
   */
  record AutomaticoPorPeriodo(PeriodoAcademico periodo) implements MotivoDeVersion {

    /** Exige el periodo. */
    public AutomaticoPorPeriodo {
      ArgumentChecks.obligatorio(periodo, "periodo");
    }
  }

  /**
   * La versión la crea el sistema porque una regla empezó o dejó de regir con la llegada de una
   * fecha.
   *
   * @param fecha fecha que la originó
   */
  record AutomaticoPorFecha(LocalDate fecha) implements MotivoDeVersion {

    /** Exige la fecha. */
    public AutomaticoPorFecha {
      ArgumentChecks.obligatorio(fecha, "fecha");
    }
  }
}
