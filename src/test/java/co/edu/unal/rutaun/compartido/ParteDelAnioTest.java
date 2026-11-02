package co.edu.unal.rutaun.compartido;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ParteDelAnioTest {

  @Test
  void tt02_parteDelAnioNumeraLaPrimeraYLaSegunda() {
    assertThat(ParteDelAnio.PRIMERO.numero()).isEqualTo(1);
    assertThat(ParteDelAnio.SEGUNDO.numero()).isEqualTo(2);
  }

  @Test
  void tt02_parteDelAnioSoloTieneDosPartes() {
    assertThat(ParteDelAnio.values()).containsExactly(ParteDelAnio.PRIMERO, ParteDelAnio.SEGUNDO);
  }
}
