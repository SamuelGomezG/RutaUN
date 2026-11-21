package co.edu.unal.rutaun.normativa.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class FacultadTest {

  @Test
  void tt02_facultadRechazaNull() {
    assertThatThrownBy(() -> new Facultad(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("codigo no puede ser null");
  }

  @Test
  void tt02_facultadRechazaCodigoVacio() {
    assertThatThrownBy(() -> new Facultad("  "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("codigo no puede ser vacío");
  }

  @Test
  void tt02_dosFacultadesConElMismoCodigoSonLaMisma() {
    Facultad una = new Facultad(" ing ");
    Facultad otra = new Facultad("ING");

    assertThat(una.codigo()).isEqualTo("ING");
    assertThat(una).isEqualTo(otra).hasSameHashCodeAs(otra);
    assertThat(una).isNotEqualTo(new Facultad("MED"));
  }
}
