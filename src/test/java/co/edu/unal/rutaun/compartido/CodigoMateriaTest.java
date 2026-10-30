package co.edu.unal.rutaun.compartido;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class CodigoMateriaTest {

  @Test
  void tt02_codigoMateriaRechazaNull() {
    assertThatThrownBy(() -> new CodigoMateria(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("valor no puede ser null");
  }

  @Test
  void tt02_codigoMateriaRechazaBlanco() {
    assertThatThrownBy(() -> new CodigoMateria("  "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("valor no puede ser vacío");
  }

  @Test
  void tt02_codigoMateriaGuardaElCodigoTalComoSeDio() {
    assertThat(new CodigoMateria("2028837").valor()).isEqualTo("2028837");
  }
}
