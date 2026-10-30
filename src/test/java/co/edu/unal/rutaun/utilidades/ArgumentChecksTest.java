package co.edu.unal.rutaun.utilidades;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ArgumentChecksTest {

  @Test
  void tt01_obligatorioRechazaNull() {
    assertThatThrownBy(() -> ArgumentChecks.obligatorio(null, "materia"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("materia no puede ser null");
  }

  @Test
  void tt01_obligatorioDevuelveElValorCuandoNoEsNull() {
    assertThat(ArgumentChecks.obligatorio("MATE-1", "materia")).isEqualTo("MATE-1");
  }

  @Test
  void tt01_obligatorioNoVacioRechazaBlanco() {
    assertThatThrownBy(() -> ArgumentChecks.obligatorioNoVacio("   ", "nombre"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("nombre no puede ser vacío");
  }

  @Test
  void tt01_obligatorioNoVacioDevuelveLaCadenaConContenido() {
    assertThat(ArgumentChecks.obligatorioNoVacio(" Cálculo 1 ", "nombre")).isEqualTo(" Cálculo 1 ");
  }

  @Test
  void tt01_obligatorioNoNegativoRechazaNegativos() {
    assertThatThrownBy(() -> ArgumentChecks.obligatorioNoNegativo(-1, "creditos"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("creditos no puede ser negativo");
  }

  @Test
  void tt01_obligatorioNoNegativoAceptaCero() {
    assertThat(ArgumentChecks.obligatorioNoNegativo(0, "creditos")).isZero();
  }

  @Test
  void tt02_obligatorioPositivoRechazaCero() {
    assertThatThrownBy(() -> ArgumentChecks.obligatorioPositivo(0, "version"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("version debe ser mayor que cero");
  }

  @Test
  void tt02_obligatorioPositivoAceptaUno() {
    assertThat(ArgumentChecks.obligatorioPositivo(1, "version")).isEqualTo(1);
  }
}
