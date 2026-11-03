package co.edu.unal.rutaun.normativa.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.rutaun.compartido.Creditos;
import co.edu.unal.rutaun.compartido.EstadoRegla;
import co.edu.unal.rutaun.compartido.Momento;
import co.edu.unal.rutaun.compartido.NormaId;
import co.edu.unal.rutaun.compartido.ParteDelAnio;
import co.edu.unal.rutaun.compartido.PeriodoAcademico;
import co.edu.unal.rutaun.compartido.ReglaId;
import co.edu.unal.rutaun.normativa.ClasificacionRegla;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ReglaTest {

  private static final NormaId NORMA = NormaId.nuevo();
  private static final Fuente FUENTE = new Fuente(NORMA, "art. 2");
  private static final Vigencia VIGENCIA =
      new Vigencia(
          new Momento.EnPeriodo(new PeriodoAcademico(2026, ParteDelAnio.SEGUNDO)),
          Optional.empty());
  private static final DatosDeRegla DATOS = new DatosDeRegla.DePrograma(new Creditos(160));
  private static final Afectacion AFECTACION =
      new Afectacion(
          TipoDeAfectacion.DEROGA,
          Optional.of(ReglaId.nuevo()),
          Optional.empty(),
          Optional.empty());

  @Test
  void tt03_reglaGuardaSusDatosYSuCategoria() {
    Regla regla =
        Regla.registrar(
            "Créditos del plan de estudios",
            "El plan de estudios exige 160 créditos.",
            FUENTE,
            Optional.of(VIGENCIA),
            ClasificacionRegla.DEL_PLAN,
            EstadoRegla.VIGENTE,
            Optional.empty(),
            DATOS);

    assertThat(regla.objeto()).isEqualTo("Créditos del plan de estudios");
    assertThat(regla.enunciado()).isEqualTo("El plan de estudios exige 160 créditos.");
    assertThat(regla.fuente()).isEqualTo(FUENTE);
    assertThat(regla.vigenciaPropia()).contains(VIGENCIA);
    assertThat(regla.clasificacion()).isEqualTo(ClasificacionRegla.DEL_PLAN);
    assertThat(regla.estado()).isEqualTo(EstadoRegla.VIGENTE);
    assertThat(regla.afectacion()).isEmpty();
    assertThat(regla.datos()).isEqualTo(DATOS);
  }

  @Test
  void tt03_reglaGuardaSuAfectacion() {
    Regla regla =
        Regla.registrar(
            "Objeto",
            "Enunciado",
            FUENTE,
            Optional.empty(),
            ClasificacionRegla.INFORMATIVA,
            EstadoRegla.VIGENTE,
            Optional.of(AFECTACION),
            DATOS);

    assertThat(regla.afectacion()).contains(AFECTACION);
  }

  @Test
  void tt03_reglaSinFuenteEsRechazada() {
    assertThatThrownBy(
            () ->
                Regla.registrar(
                    "Objeto",
                    "Enunciado",
                    null,
                    Optional.empty(),
                    ClasificacionRegla.INFORMATIVA,
                    EstadoRegla.VIGENTE,
                    Optional.empty(),
                    DATOS))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("fuente no puede ser null");
  }

  @Test
  void tt03_reglaRechazaDatosObligatoriosNulos() {
    assertThatThrownBy(
            () ->
                Regla.registrar(
                    " ",
                    "Enunciado",
                    FUENTE,
                    Optional.empty(),
                    ClasificacionRegla.INFORMATIVA,
                    EstadoRegla.VIGENTE,
                    Optional.empty(),
                    DATOS))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("objeto no puede ser vacío");
    assertThatThrownBy(
            () ->
                Regla.registrar(
                    "Objeto",
                    " ",
                    FUENTE,
                    Optional.empty(),
                    ClasificacionRegla.INFORMATIVA,
                    EstadoRegla.VIGENTE,
                    Optional.empty(),
                    DATOS))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("enunciado no puede ser vacío");
    assertThatThrownBy(
            () ->
                Regla.registrar(
                    "Objeto",
                    "Enunciado",
                    FUENTE,
                    null,
                    ClasificacionRegla.INFORMATIVA,
                    EstadoRegla.VIGENTE,
                    Optional.empty(),
                    DATOS))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("vigenciaPropia no puede ser null");
    assertThatThrownBy(
            () ->
                Regla.registrar(
                    "Objeto",
                    "Enunciado",
                    FUENTE,
                    Optional.empty(),
                    null,
                    EstadoRegla.VIGENTE,
                    Optional.empty(),
                    DATOS))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("clasificacion no puede ser null");
    assertThatThrownBy(
            () ->
                Regla.registrar(
                    "Objeto",
                    "Enunciado",
                    FUENTE,
                    Optional.empty(),
                    ClasificacionRegla.INFORMATIVA,
                    null,
                    Optional.empty(),
                    DATOS))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("estado no puede ser null");
    assertThatThrownBy(
            () ->
                Regla.registrar(
                    "Objeto",
                    "Enunciado",
                    FUENTE,
                    Optional.empty(),
                    ClasificacionRegla.INFORMATIVA,
                    EstadoRegla.VIGENTE,
                    null,
                    DATOS))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("afectacion no puede ser null");
    assertThatThrownBy(
            () ->
                Regla.registrar(
                    "Objeto",
                    "Enunciado",
                    FUENTE,
                    Optional.empty(),
                    ClasificacionRegla.INFORMATIVA,
                    EstadoRegla.VIGENTE,
                    Optional.empty(),
                    null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("datos no puede ser null");
  }

  @Test
  void tt03_reglaTemporalNoPuedeSerDelPlan() {
    Vigencia temporal =
        new Vigencia(
            new Momento.EnPeriodo(new PeriodoAcademico(2026, ParteDelAnio.SEGUNDO)),
            Optional.of(new Momento.EnPeriodo(new PeriodoAcademico(2026, ParteDelAnio.SEGUNDO))));

    assertThatThrownBy(
            () ->
                Regla.registrar(
                    "Objeto",
                    "Enunciado",
                    FUENTE,
                    Optional.of(temporal),
                    ClasificacionRegla.DEL_PLAN,
                    EstadoRegla.VIGENTE,
                    Optional.empty(),
                    DATOS))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("una regla temporal no puede ser regla del plan");
  }

  @Test
  void tt03_reglaSeIdentificaPorSuId() {
    ReglaId id = ReglaId.nuevo();
    Regla una = reconstituir(id);
    Regla otra = reconstituir(id);

    assertThat(una).isEqualTo(otra).hasSameHashCodeAs(otra);
    assertThat(una).isNotEqualTo(reconstituir(ReglaId.nuevo()));
  }

  private static Regla reconstituir(ReglaId id) {
    return Regla.reconstituir(
        id,
        "Objeto",
        "Enunciado",
        FUENTE,
        Optional.empty(),
        ClasificacionRegla.INFORMATIVA,
        EstadoRegla.VIGENTE,
        Optional.empty(),
        DATOS);
  }
}
