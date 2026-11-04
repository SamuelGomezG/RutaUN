package co.edu.unal.rutaun.normativa.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.context.annotation.Profile;

/**
 * Verifica el runner que carga la semilla en desarrollo, sin arrancar Spring.
 *
 * <p>SOP-03 (#55).
 */
class SemillaDeDesarrolloRunnerTest {

  @Test
  void sop03_elRunnerEnviaAlServicioElArchivoRealDeProgramas() {
    ImportacionDeProgramasService servicio = mock(ImportacionDeProgramasService.class);
    when(servicio.importar(anyString())).thenReturn(new ResultadoDeCargaDeProgramas(1, 0));
    SemillaDeDesarrolloRunner runner = new SemillaDeDesarrolloRunner(servicio);

    runner.run(new DefaultApplicationArguments());

    ArgumentCaptor<String> json = ArgumentCaptor.forClass(String.class);
    verify(servicio).importar(json.capture());
    assertThat(json.getValue()).contains("2879");
  }

  @Test
  void sop03_siElArchivoEsInvalidoElArranqueFalla() {
    ImportacionDeProgramasService servicio = mock(ImportacionDeProgramasService.class);
    when(servicio.importar(anyString()))
        .thenThrow(new ArchivoDeProgramasInvalidoException("archivo inválido"));
    SemillaDeDesarrolloRunner runner = new SemillaDeDesarrolloRunner(servicio);

    assertThatThrownBy(() -> runner.run(new DefaultApplicationArguments()))
        .isInstanceOf(ArchivoDeProgramasInvalidoException.class);
  }

  @Test
  void sop03_elRunnerSoloSeActivaConElPerfilDev() {
    Profile perfil = SemillaDeDesarrolloRunner.class.getAnnotation(Profile.class);

    assertThat(perfil.value()).containsExactly("dev");
  }
}
