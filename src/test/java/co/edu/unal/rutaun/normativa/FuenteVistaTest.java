package co.edu.unal.rutaun.normativa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class FuenteVistaTest {

  private static final NormaVista NORMA =
      new NormaVista(TipoNorma.ACUERDO, "11", 2023, "Consejo de Facultad de Ingeniería");

  @Test
  void tt02_fuenteVistaRechazaNormaNulaIArticuloVacio() {
    assertThatThrownBy(() -> new FuenteVista(null, "art. 1", Optional.empty()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("norma no puede ser null");
    assertThatThrownBy(() -> new FuenteVista(NORMA, "", Optional.empty()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("articulo no puede ser vacío");
    assertThatThrownBy(() -> new FuenteVista(NORMA, "art. 1", null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("enlace no puede ser null");
  }

  @Test
  void tt02_fuenteVistaRechazaUnEnlaceVacio() {
    assertThatThrownBy(() -> new FuenteVista(NORMA, "art. 1", Optional.of(" ")))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("enlace no puede ser vacío");
  }

  @Test
  void tt02_fuenteVistaAceptaUnEnlaceAusente() {
    FuenteVista fuente = new FuenteVista(NORMA, "art. 2 (transitorio)", Optional.empty());

    assertThat(fuente.norma()).isEqualTo(NORMA);
    assertThat(fuente.articulo()).isEqualTo("art. 2 (transitorio)");
    assertThat(fuente.enlace()).isEmpty();
  }

  @Test
  void tt02_fuenteVistaLlevaElEnlaceCuandoExiste() {
    FuenteVista fuente =
        new FuenteVista(
            NORMA, "art. 1", Optional.of("https://regimenlegal.unal.edu.co/acuerdo-11-2023"));

    assertThat(fuente.enlace()).contains("https://regimenlegal.unal.edu.co/acuerdo-11-2023");
  }
}
