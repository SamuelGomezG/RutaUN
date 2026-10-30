package co.edu.unal.rutaun.compartido;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class CreditosTest {

  @Test
  void tt02_creditosRechazaNegativos() {
    assertThatThrownBy(() -> new Creditos(-1))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("valor no puede ser negativo");
  }

  @Test
  void tt02_creditosAceptaCero() {
    assertThat(new Creditos(0).valor()).isZero();
  }

  @Test
  void tt02_creditosSumaConOtros() {
    assertThat(new Creditos(3).suma(new Creditos(4))).isEqualTo(new Creditos(7));
  }

  @Test
  void tt02_creditosRechazaSumarNull() {
    assertThatThrownBy(() -> new Creditos(3).suma(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("otros no puede ser null");
  }
}
