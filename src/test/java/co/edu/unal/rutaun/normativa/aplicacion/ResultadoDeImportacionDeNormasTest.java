package co.edu.unal.rutaun.normativa.aplicacion;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ResultadoDeImportacionDeNormasTest {

  @Test
  void uc07_ext5a_unaNormaQueNoSeIncorporoNoPuedeLlevarElAviso() {
    assertThatThrownBy(
            () -> new ResultadoDeImportacionDeNormas(false, "ACUERDO 003-A de 2022", 0, true))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("no se incorporó");
  }
}
