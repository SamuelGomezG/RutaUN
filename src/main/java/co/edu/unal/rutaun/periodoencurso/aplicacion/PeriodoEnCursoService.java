package co.edu.unal.rutaun.periodoencurso.aplicacion;

import co.edu.unal.rutaun.compartido.PeriodoAcademico;
import co.edu.unal.rutaun.periodoencurso.PeriodoEnCursoFacade;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Periodo en curso tomado de la propiedad {@code rutaun.periodo-en-curso} de application.yml, que
 * es el único punto donde se cambia. Se lee y se valida al arrancar: si falta o no tiene el formato
 * AAAA-1 o AAAA-2, la aplicación no arranca. Se ignoran los espacios al inicio y al final.
 */
@Component
public class PeriodoEnCursoService implements PeriodoEnCursoFacade {

  private final PeriodoAcademico periodo;

  /**
   * Lee y valida el periodo configurado.
   *
   * @param texto el valor de {@code rutaun.periodo-en-curso}, por ejemplo «2026-2»
   * @throws IllegalArgumentException si el valor no tiene el formato AAAA-1 o AAAA-2
   */
  public PeriodoEnCursoService(@Value("${rutaun.periodo-en-curso}") String texto) {
    try {
      this.periodo = PeriodoAcademico.desdeTexto(texto);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(
          "rutaun.periodo-en-curso no es válida («" + texto + "»): " + e.getMessage(), e);
    }
  }

  @Override
  public PeriodoAcademico actual() {
    return periodo;
  }
}
