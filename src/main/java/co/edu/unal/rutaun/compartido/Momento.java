package co.edu.unal.rutaun.compartido;

import co.edu.unal.rutaun.utilidades.ArgumentChecks;
import java.time.LocalDate;

/**
 * Momento en que algo empieza o deja de regir: un periodo académico o una fecha. Sirve para las
 * fechas de aplicación y las «vigencia hasta» de normas y reglas (glosario, «Vigencia»). No es
 * comparable: RutaUN no guarda las fechas de los periodos (S4), así que no puede decir si un
 * periodo cae antes o después de una fecha.
 */
public sealed interface Momento {

  /**
   * Momento dado por un periodo académico.
   *
   * @param periodo el periodo
   */
  record EnPeriodo(PeriodoAcademico periodo) implements Momento {

    /** Exige el periodo. */
    public EnPeriodo {
      ArgumentChecks.obligatorio(periodo, "periodo");
    }
  }

  /**
   * Momento dado por una fecha.
   *
   * @param fecha la fecha
   */
  record EnFecha(LocalDate fecha) implements Momento {

    /** Exige la fecha. */
    public EnFecha {
      ArgumentChecks.obligatorio(fecha, "fecha");
    }
  }
}
