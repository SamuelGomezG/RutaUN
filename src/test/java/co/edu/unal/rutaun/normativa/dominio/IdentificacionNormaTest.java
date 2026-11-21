package co.edu.unal.rutaun.normativa.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.rutaun.normativa.TipoNorma;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class IdentificacionNormaTest {

  private static final Organo CONSEJO_ACADEMICO =
      new Organo("Consejo Académico", NivelDeOrgano.NACIONAL, Optional.empty());
  private static final Organo CONSEJO_SUPERIOR =
      new Organo("Consejo Superior", NivelDeOrgano.NACIONAL, Optional.empty());

  @Test
  void tt03_identificacionNormaGuardaTipoNumeroAnioYOrgano() {
    IdentificacionNorma identificacion =
        new IdentificacionNorma(TipoNorma.ACUERDO, "003-A", 2022, CONSEJO_ACADEMICO);

    assertThat(identificacion.tipo()).isEqualTo(TipoNorma.ACUERDO);
    assertThat(identificacion.numero()).isEqualTo("003-A");
    assertThat(identificacion.anio()).isEqualTo(2022);
    assertThat(identificacion.organo()).isEqualTo(CONSEJO_ACADEMICO);
  }

  @Test
  void tt03_identificacionNormaRechazaDatosInvalidos() {
    assertThatThrownBy(() -> new IdentificacionNorma(null, "003-A", 2022, CONSEJO_ACADEMICO))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("tipo no puede ser null");
    assertThatThrownBy(
            () -> new IdentificacionNorma(TipoNorma.ACUERDO, " ", 2022, CONSEJO_ACADEMICO))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("numero no puede ser vacío");
    assertThatThrownBy(
            () -> new IdentificacionNorma(TipoNorma.ACUERDO, "003-A", 0, CONSEJO_ACADEMICO))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("anio debe ser mayor que cero");
    assertThatThrownBy(() -> new IdentificacionNorma(TipoNorma.ACUERDO, "003-A", 2022, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("organo no puede ser null");
  }

  @Test
  void tt03_identificacionNormaSeIgualaPorSusCuatroDatos() {
    IdentificacionNorma una =
        new IdentificacionNorma(TipoNorma.ACUERDO, "003-A", 2022, CONSEJO_ACADEMICO);
    IdentificacionNorma otra =
        new IdentificacionNorma(TipoNorma.ACUERDO, "003-A", 2022, CONSEJO_ACADEMICO);
    IdentificacionNorma distinta =
        new IdentificacionNorma(TipoNorma.ACUERDO, "003-A", 2022, CONSEJO_SUPERIOR);

    assertThat(una).isEqualTo(otra).hasSameHashCodeAs(otra);
    assertThat(una).isNotEqualTo(distinta);
  }

  @Test
  void uc07_ext4c_elNumeroQuedaEnUnaSolaForma() {
    assertThat(identificacion("003A").numero()).isEqualTo("003-A");
    assertThat(identificacion("003-A").numero()).isEqualTo("003-A");
    assertThat(identificacion("003 a").numero()).isEqualTo("003-A");
    assertThat(identificacion(" 003-a ").numero()).isEqualTo("003-A");
    assertThat(identificacion("008").numero()).isEqualTo("008");
    assertThat(identificacion("A-12").numero()).isEqualTo("A-12");
  }

  private static IdentificacionNorma identificacion(String numero) {
    return new IdentificacionNorma(TipoNorma.ACUERDO, numero, 2022, CONSEJO_ACADEMICO);
  }
}
