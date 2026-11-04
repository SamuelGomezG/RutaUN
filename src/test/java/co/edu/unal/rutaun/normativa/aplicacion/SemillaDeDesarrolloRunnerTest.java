package co.edu.unal.rutaun.normativa.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.context.annotation.Profile;

/**
 * Verifica el runner que carga la semilla en desarrollo, sin arrancar Spring.
 *
 * <p>SOP-03 (#55).
 */
class SemillaDeDesarrolloRunnerTest {

  @Test
  void uc07_hu03_t4_elRunnerEnviaAlServicioElArchivoRealDeProgramasYDespuesLasNormas() {
    ImportacionDeProgramasService programas = mock(ImportacionDeProgramasService.class);
    ImportacionDeNormasService normas = mock(ImportacionDeNormasService.class);
    when(programas.importar(anyString())).thenReturn(new ResultadoDeCargaDeProgramas(1, 0));
    when(normas.importar(any(InputStream.class)))
        .thenReturn(new ResultadoDeImportacionDeNormas(true, "ACUERDO 003-A de 2022", 1));
    SemillaDeDesarrolloRunner runner = new SemillaDeDesarrolloRunner(programas, normas);

    runner.run(new DefaultApplicationArguments());

    ArgumentCaptor<String> json = ArgumentCaptor.forClass(String.class);
    verify(programas).importar(json.capture());
    assertThat(json.getValue()).contains("2879");
    InOrder orden = inOrder(programas, normas);
    orden.verify(programas).importar(anyString());
    orden.verify(normas).importar(any(InputStream.class));
  }

  @Test
  void sop03_siElArchivoEsInvalidoElArranqueFalla() {
    ImportacionDeProgramasService programas = mock(ImportacionDeProgramasService.class);
    ImportacionDeNormasService normas = mock(ImportacionDeNormasService.class);
    when(programas.importar(anyString()))
        .thenThrow(new ArchivoDeProgramasInvalidoException("archivo inválido"));
    SemillaDeDesarrolloRunner runner = new SemillaDeDesarrolloRunner(programas, normas);

    assertThatThrownBy(() -> runner.run(new DefaultApplicationArguments()))
        .isInstanceOf(ArchivoDeProgramasInvalidoException.class);
  }

  @Test
  void uc07_hu03_t4_elRunnerSoloSeActivaConElPerfilDev() {
    Profile perfil = SemillaDeDesarrolloRunner.class.getAnnotation(Profile.class);

    assertThat(perfil.value()).containsExactly("dev");
  }
}
