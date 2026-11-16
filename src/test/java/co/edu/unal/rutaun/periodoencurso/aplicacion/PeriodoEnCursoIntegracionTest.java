package co.edu.unal.rutaun.periodoencurso.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.unal.rutaun.compartido.ParteDelAnio;
import co.edu.unal.rutaun.compartido.PeriodoAcademico;
import co.edu.unal.rutaun.periodoencurso.PeriodoEnCursoFacade;
import co.edu.unal.rutaun.soporte.PruebaDeIntegracion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

@PruebaDeIntegracion
@TestPropertySource(properties = "rutaun.periodo-en-curso=2027-1")
class PeriodoEnCursoIntegracionTest {

  private final PeriodoEnCursoFacade periodoEnCurso;

  @Autowired
  PeriodoEnCursoIntegracionTest(PeriodoEnCursoFacade periodoEnCurso) {
    this.periodoEnCurso = periodoEnCurso;
  }

  @Test
  void sop04_periodoEnCursoSeLeeDeLaConfiguracionAlArrancar() {
    assertThat(periodoEnCurso.actual()).isEqualTo(new PeriodoAcademico(2027, ParteDelAnio.PRIMERO));
  }
}
