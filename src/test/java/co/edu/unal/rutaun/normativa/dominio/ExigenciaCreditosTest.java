package co.edu.unal.rutaun.normativa.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.rutaun.compartido.Creditos;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ExigenciaCreditosTest {

  @Test
  void tt02_exigenciaSinDesgloseGuardaSoloLosCreditosExigidos() {
    ExigenciaCreditos exigencia = ExigenciaCreditos.de(new Creditos(16));

    assertThat(exigencia.exigidos()).isEqualTo(new Creditos(16));
    assertThat(exigencia.desglose()).isEmpty();
  }

  @Test
  void tt02_exigenciaConDesgloseSumaObligatoriosYOptativos() {
    ExigenciaCreditos exigencia = ExigenciaCreditos.conDesglose(new Creditos(15), new Creditos(36));

    assertThat(exigencia.exigidos()).isEqualTo(new Creditos(51));
    assertThat(exigencia.desglose())
        .contains(new ExigenciaCreditos.Desglose(new Creditos(15), new Creditos(36)));
  }

  @Test
  void tt02_exigenciaRechazaUnDesgloseQueNoSumaLoExigido() {
    ExigenciaCreditos.Desglose desglose =
        new ExigenciaCreditos.Desglose(new Creditos(15), new Creditos(36));

    assertThatThrownBy(() -> new ExigenciaCreditos(new Creditos(50), Optional.of(desglose)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("el desglose debe sumar los créditos exigidos");
  }

  @Test
  void tt02_exigenciaRechazaDatosNull() {
    assertThatThrownBy(() -> ExigenciaCreditos.de(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("exigidos no puede ser null");
    assertThatThrownBy(() -> new ExigenciaCreditos(new Creditos(1), null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("desglose no puede ser null");
    assertThatThrownBy(() -> new ExigenciaCreditos.Desglose(null, new Creditos(1)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("obligatorios no puede ser null");
    assertThatThrownBy(() -> new ExigenciaCreditos.Desglose(new Creditos(1), null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("optativos no puede ser null");
  }
}
