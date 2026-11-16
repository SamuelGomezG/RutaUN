package co.edu.unal.rutaun.periodoencurso.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.unal.rutaun.compartido.ParteDelAnio;
import co.edu.unal.rutaun.compartido.PeriodoAcademico;
import co.edu.unal.rutaun.periodoencurso.PeriodoEnCursoFacade;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

class PeriodoEnCursoArranqueTest {

  private final ApplicationContextRunner arranque =
      new ApplicationContextRunner()
          .withBean(PropertySourcesPlaceholderConfigurer.class)
          .withUserConfiguration(PeriodoEnCursoService.class);

  @Test
  void sop04_elArranqueLeeUnPeriodoValido() {
    arranque
        .withPropertyValues("rutaun.periodo-en-curso=2027-1")
        .run(
            contexto -> {
              assertThat(contexto).hasNotFailed();
              assertThat(contexto.getBean(PeriodoEnCursoFacade.class).actual())
                  .isEqualTo(new PeriodoAcademico(2027, ParteDelAnio.PRIMERO));
            });
  }

  @Test
  void sop04_elArranqueFallaConUnPeriodoInvalido() {
    arranque
        .withPropertyValues("rutaun.periodo-en-curso=2026-3")
        .run(
            contexto -> {
              assertThat(contexto).hasFailed();
              assertThat(contexto.getStartupFailure())
                  .hasStackTraceContaining("rutaun.periodo-en-curso")
                  .hasStackTraceContaining("AAAA-1 o AAAA-2");
            });
  }

  @Test
  void sop04_elArranqueFallaSiFaltaElPeriodo() {
    arranque.run(
        contexto -> {
          assertThat(contexto).hasFailed();
          assertThat(contexto.getStartupFailure())
              .hasStackTraceContaining("rutaun.periodo-en-curso");
        });
  }
}
