package co.edu.unal.rutaun.soporte;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.unal.rutaun.compartido.ProgramaId;
import co.edu.unal.rutaun.normativa.ConsolidadoVista;
import co.edu.unal.rutaun.normativa.ReglaVista;
import java.util.List;
import org.junit.jupiter.api.Test;

class ConsultaNormativaFalsaTest {

  @Test
  void tt02_laFachadaFalsaOfreceSistemasConTresReglas() {
    ConsultaNormativaFalsa fachada = ConsultaNormativaFalsa.conReglas();

    assertThat(fachada.programas()).hasSize(1);
    assertThat(fachada.programa(ConsultaNormativaFalsa.SISTEMAS))
        .hasValueSatisfying(
            p -> assertThat(p.nombre()).isEqualTo("Ingeniería de Sistemas y Computación"));
    ConsolidadoVista consolidado =
        fachada.consolidadoVigente(ConsultaNormativaFalsa.SISTEMAS).orElseThrow();
    assertThat(consolidado.version()).isEqualTo(1);
    assertThat(consolidado.reglasPorCategoria().values().stream().mapToInt(List::size).sum())
        .isEqualTo(3);
    assertThat(
            consolidado.reglasPorCategoria().values().stream()
                .flatMap(List::stream)
                .filter(ReglaVista::temporal))
        .hasSize(1);
    assertThat(
            consolidado.reglasPorCategoria().values().stream()
                .flatMap(List::stream)
                .allMatch(regla -> regla.fuente().isPresent()))
        .isTrue();
  }

  @Test
  void tt02_laFachadaFalsaPublicaLasCategoriasEnOrdenDelConsolidado() {
    ConsolidadoVista consolidado =
        ConsultaNormativaFalsa.conReglas()
            .consolidadoVigente(ConsultaNormativaFalsa.SISTEMAS)
            .orElseThrow();

    assertThat(consolidado.reglasPorCategoria().keySet())
        .containsExactly("Carga por periodo", "Prerrequisito", "Informativa");
  }

  @Test
  void tt02_laFachadaFalsaSinReglasNoTieneConsolidado() {
    ConsultaNormativaFalsa fachada = ConsultaNormativaFalsa.sinReglas();

    assertThat(fachada.programa(ConsultaNormativaFalsa.SISTEMAS)).isPresent();
    assertThat(fachada.consolidadoVigente(ConsultaNormativaFalsa.SISTEMAS)).isEmpty();
  }

  @Test
  void tt02_laFachadaFalsaNoConoceOtrosProgramas() {
    ProgramaId otro = ProgramaId.nuevo();

    assertThat(ConsultaNormativaFalsa.conReglas().programa(otro)).isEmpty();
    assertThat(ConsultaNormativaFalsa.conReglas().consolidadoVigente(otro)).isEmpty();
  }
}
