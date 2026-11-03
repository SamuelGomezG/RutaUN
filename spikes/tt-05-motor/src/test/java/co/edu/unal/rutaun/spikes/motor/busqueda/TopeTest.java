package co.edu.unal.rutaun.spikes.motor.busqueda;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class TopeTest {

  @Test
  void tt05_unTopeSinLimiteEsValido() {
    Tope tope = new Tope("T4", null, null);
    assertThat(tope.limiteCreditos()).isEqualTo(Integer.MAX_VALUE);
    assertThat(tope.limiteMaterias()).isEqualTo(Integer.MAX_VALUE);
  }

  @Test
  void tt05_unTopeDeCreditosNoPositivoSeRechaza() {
    assertThatThrownBy(() -> new Tope("X", 0, 5))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("créditos");
  }

  @Test
  void tt05_unTopeDeMateriasNegativoSeRechaza() {
    assertThatThrownBy(() -> new Tope("X", 18, -1))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("materias");
  }
}
