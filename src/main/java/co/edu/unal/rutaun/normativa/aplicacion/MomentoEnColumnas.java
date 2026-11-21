package co.edu.unal.rutaun.normativa.aplicacion;

import co.edu.unal.rutaun.compartido.Momento;
import co.edu.unal.rutaun.compartido.PeriodoAcademico;
import co.edu.unal.rutaun.normativa.persistencia.ColumnasDeMomento;
import co.edu.unal.rutaun.utilidades.ArgumentChecks;

/** Convierte un momento en sus tres columnas y de vuelta. */
final class MomentoEnColumnas {

  static final String FECHA = "FECHA";
  static final String PERIODO = "PERIODO";

  private MomentoEnColumnas() {}

  static ColumnasDeMomento aColumnas(Momento momento) {
    if (momento == null) {
      return new ColumnasDeMomento(null, null, null);
    }
    return switch (momento) {
      case Momento.EnFecha enFecha -> new ColumnasDeMomento(FECHA, enFecha.fecha(), null);
      case Momento.EnPeriodo enPeriodo ->
          new ColumnasDeMomento(PERIODO, null, enPeriodo.periodo().toString());
    };
  }

  static Momento aMomento(ColumnasDeMomento columnas) {
    if (columnas == null || columnas.tipo() == null) {
      return null;
    }
    return switch (ArgumentChecks.obligatorioNoVacio(columnas.tipo(), "tipo")) {
      case FECHA -> new Momento.EnFecha(columnas.fecha());
      case PERIODO -> new Momento.EnPeriodo(PeriodoAcademico.desdeTexto(columnas.periodo()));
      default ->
          throw new IllegalArgumentException("tipo de momento desconocido: " + columnas.tipo());
    };
  }
}
