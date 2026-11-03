package co.edu.unal.rutaun.normativa.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.rutaun.compartido.NormaId;
import org.junit.jupiter.api.Test;

class FuenteTest {

  private static final NormaId NORMA = NormaId.nuevo();

  @Test
  void tt03_fuenteGuardaLaNormaYElArticuloONumeral() {
    Fuente fuente = new Fuente(NORMA, "art. 2 (transitorio)");

    assertThat(fuente.norma()).isEqualTo(NORMA);
    assertThat(fuente.articulo()).isEqualTo("art. 2 (transitorio)");
  }

  @Test
  void tt03_fuenteRechazaNormaNulaYArticuloVacio() {
    assertThatThrownBy(() -> new Fuente(null, "art. 2"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("norma no puede ser null");
    assertThatThrownBy(() -> new Fuente(NORMA, " "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("articulo no puede ser vacío");
  }
}
