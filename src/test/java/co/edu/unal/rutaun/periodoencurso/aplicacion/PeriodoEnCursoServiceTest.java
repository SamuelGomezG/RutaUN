package co.edu.unal.rutaun.periodoencurso.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.rutaun.compartido.ParteDelAnio;
import co.edu.unal.rutaun.compartido.PeriodoAcademico;
import org.junit.jupiter.api.Test;

class PeriodoEnCursoServiceTest {

  @Test
  void sop04_periodoEnCursoDevuelveElPeriodoConfigurado() {
    PeriodoEnCursoService periodoEnCurso = new PeriodoEnCursoService("2026-2");

    assertThat(periodoEnCurso.actual()).isEqualTo(new PeriodoAcademico(2026, ParteDelAnio.SEGUNDO));
  }

  @Test
  void sop04_periodoEnCursoRechazaUnFormatoInvalidoAlCrearse() {
    assertThatThrownBy(() -> new PeriodoEnCursoService("2026-3"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("rutaun.periodo-en-curso")
        .hasMessageContaining("AAAA-1 o AAAA-2");
  }
}
