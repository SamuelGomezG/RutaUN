package co.edu.unal.rutaun.normativa.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.rutaun.normativa.TipoNorma;
import org.junit.jupiter.api.Test;

class IdentificacionNormaTest {

  @Test
  void tt03_identificacionNormaGuardaTipoNumeroAnioYOrgano() {
    IdentificacionNorma identificacion =
        new IdentificacionNorma(TipoNorma.ACUERDO, "003-A", 2022, "Consejo Académico");

    assertThat(identificacion.tipo()).isEqualTo(TipoNorma.ACUERDO);
    assertThat(identificacion.numero()).isEqualTo("003-A");
    assertThat(identificacion.anio()).isEqualTo(2022);
    assertThat(identificacion.organo()).isEqualTo("Consejo Académico");
  }

  @Test
  void tt03_identificacionNormaRechazaDatosInvalidos() {
    assertThatThrownBy(() -> new IdentificacionNorma(null, "003-A", 2022, "Consejo Académico"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("tipo no puede ser null");
    assertThatThrownBy(
            () -> new IdentificacionNorma(TipoNorma.ACUERDO, " ", 2022, "Consejo Académico"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("numero no puede ser vacío");
    assertThatThrownBy(
            () -> new IdentificacionNorma(TipoNorma.ACUERDO, "003-A", 0, "Consejo Académico"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("anio debe ser mayor que cero");
    assertThatThrownBy(() -> new IdentificacionNorma(TipoNorma.ACUERDO, "003-A", 2022, ""))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("organo no puede ser vacío");
  }

  @Test
  void tt03_identificacionNormaSeIgualaPorSusCuatroDatos() {
    IdentificacionNorma una =
        new IdentificacionNorma(TipoNorma.ACUERDO, "003-A", 2022, "Consejo Académico");
    IdentificacionNorma otra =
        new IdentificacionNorma(TipoNorma.ACUERDO, "003-A", 2022, "Consejo Académico");
    IdentificacionNorma distinta =
        new IdentificacionNorma(TipoNorma.ACUERDO, "003-A", 2022, "Consejo Superior");

    assertThat(una).isEqualTo(otra).hasSameHashCodeAs(otra);
    assertThat(una).isNotEqualTo(distinta);
  }
}
