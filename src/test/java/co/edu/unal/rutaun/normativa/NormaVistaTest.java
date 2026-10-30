package co.edu.unal.rutaun.normativa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class NormaVistaTest {

  @Test
  void tt02_normaVistaRechazaDatosVaciosONulos() {
    assertThatThrownBy(() -> new NormaVista(null, "11", 2023, "Consejo de Facultad"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("tipo no puede ser null");
    assertThatThrownBy(() -> new NormaVista(TipoNorma.ACUERDO, " ", 2023, "Consejo de Facultad"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("numero no puede ser vacío");
    assertThatThrownBy(() -> new NormaVista(TipoNorma.ACUERDO, "11", 0, "Consejo de Facultad"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("anio debe ser mayor que cero");
    assertThatThrownBy(() -> new NormaVista(TipoNorma.ACUERDO, "11", 2023, ""))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("organo no puede ser vacío");
  }

  @Test
  void tt02_normaVistaGuardaLaIdentificacionTalComoSeDio() {
    NormaVista norma =
        new NormaVista(TipoNorma.ACUERDO, "003-A", 2022, "Consejo Superior Universitario");

    assertThat(norma.tipo()).isEqualTo(TipoNorma.ACUERDO);
    assertThat(norma.numero()).isEqualTo("003-A");
    assertThat(norma.anio()).isEqualTo(2022);
    assertThat(norma.organo()).isEqualTo("Consejo Superior Universitario");
  }
}
