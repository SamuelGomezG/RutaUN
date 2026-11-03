package co.edu.unal.rutaun.spikes.motor.medicion;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MedirTest {

  @Test
  void tt05_unMotivoConComasNoAgregaColumnasAlCsv() {
    assertThat(Medir.campoCsv("sin plan: Introducción, a la ingeniería"))
        .isEqualTo("sin plan: Introducción; a la ingeniería");
  }

  @Test
  void tt05_unMotivoConSaltosDeLineaQuedaEnUnaSolaFila() {
    assertThat(Medir.campoCsv("uno\r\ndos\ntres")).isEqualTo("uno  dos tres");
  }

  @Test
  void tt05_unCampoNuloSeEscribeVacio() {
    assertThat(Medir.campoCsv(null)).isEmpty();
  }
}
