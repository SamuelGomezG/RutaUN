package co.edu.unal.rutaun.normativa.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.rutaun.compartido.Momento;
import co.edu.unal.rutaun.compartido.NormaId;
import co.edu.unal.rutaun.compartido.ParteDelAnio;
import co.edu.unal.rutaun.compartido.PeriodoAcademico;
import co.edu.unal.rutaun.compartido.ReglaId;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class AfectacionTest {

  private static final NormaId NORMA = NormaId.nuevo();
  private static final ReglaId REGLA = ReglaId.nuevo();
  private static final Momento DESDE =
      new Momento.EnPeriodo(new PeriodoAcademico(2026, ParteDelAnio.SEGUNDO));

  @Test
  void tt03_afectacionPuedeSenalarUnaReglaConcreta() {
    Afectacion afectacion =
        new Afectacion(
            TipoDeAfectacion.DEROGA, Optional.of(REGLA), Optional.empty(), Optional.of(DESDE));

    assertThat(afectacion.tipo()).isEqualTo(TipoDeAfectacion.DEROGA);
    assertThat(afectacion.reglaAfectada()).contains(REGLA);
    assertThat(afectacion.articuloAfectado()).isEmpty();
    assertThat(afectacion.desde()).contains(DESDE);
  }

  @Test
  void tt03_afectacionPuedeSenalarTodasLasReglasDeUnArticuloDeOtraNorma() {
    Fuente articulo = new Fuente(NORMA, "art. 2");
    Afectacion afectacion =
        new Afectacion(
            TipoDeAfectacion.MODIFICA, Optional.empty(), Optional.of(articulo), Optional.empty());

    assertThat(afectacion.reglaAfectada()).isEmpty();
    assertThat(afectacion.articuloAfectado()).contains(articulo);
    assertThat(afectacion.articuloAfectado().orElseThrow().norma()).isEqualTo(NORMA);
  }

  @Test
  void tt03_afectacionExigeExactamenteUnDestino() {
    assertThatThrownBy(
            () ->
                new Afectacion(
                    TipoDeAfectacion.EXCEPTUA,
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("la afectación debe indicar exactamente una regla o un artículo");
    assertThatThrownBy(
            () ->
                new Afectacion(
                    TipoDeAfectacion.EXCEPTUA,
                    Optional.of(REGLA),
                    Optional.of(new Fuente(NORMA, "art. 2")),
                    Optional.empty()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("la afectación debe indicar exactamente una regla o un artículo");
  }

  @Test
  void tt03_afectacionRechazaDatosObligatoriosInvalidos() {
    assertThatThrownBy(
            () -> new Afectacion(null, Optional.of(REGLA), Optional.empty(), Optional.empty()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("tipo no puede ser null");
    assertThatThrownBy(
            () -> new Afectacion(TipoDeAfectacion.DEROGA, null, Optional.empty(), Optional.empty()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("reglaAfectada no puede ser null");
    assertThatThrownBy(
            () -> new Afectacion(TipoDeAfectacion.DEROGA, Optional.empty(), null, Optional.empty()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("articuloAfectado no puede ser null");
    assertThatThrownBy(
            () ->
                new Afectacion(TipoDeAfectacion.DEROGA, Optional.of(REGLA), Optional.empty(), null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("desde no puede ser null");
    assertThatThrownBy(
            () ->
                new Afectacion(
                    TipoDeAfectacion.DEROGA,
                    Optional.empty(),
                    Optional.of(new Fuente(NORMA, " ")),
                    Optional.empty()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("articulo no puede ser vacío");
  }
}
