package co.edu.unal.rutaun.normativa.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.rutaun.compartido.EstadoRegla;
import co.edu.unal.rutaun.compartido.Momento;
import co.edu.unal.rutaun.compartido.NormaId;
import co.edu.unal.rutaun.compartido.ProgramaId;
import co.edu.unal.rutaun.normativa.ClasificacionRegla;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * UC07 ext. 6f y UC07-HU03-T4 (#130): el validador del consolidado es el único camino para obtener
 * un {@link ConsolidadoValidado}. El lector genera un id nuevo por regla, así que el rechazo por id
 * repetido no se puede provocar desde un archivo: estas pruebas ejercitan la invariante del
 * validador.
 */
class ValidadorDelConsolidadoTest {

  private static final ProgramaId PROGRAMA = ProgramaId.nuevo();
  private static final NormaId NORMA = NormaId.nuevo();
  private static final LocalDate FECHA = LocalDate.of(2026, 11, 21);
  private static final MotivoDeVersion MOTIVO = new MotivoDeVersion.PorIncorporacion(NORMA, "Ana");
  private static final Vigencia PERMANENTE =
      new Vigencia(new Momento.EnFecha(FECHA), Optional.empty());
  private static final Vigencia TEMPORAL =
      new Vigencia(
          new Momento.EnFecha(FECHA), Optional.of(new Momento.EnFecha(FECHA.plusYears(1))));

  @Test
  void uc07_ext6f_unaReglaTemporalDelPlanNoPasaElValidador() {
    ReglaEnVersion nueva =
        enVersion("Máximo de créditos", "art. 2", ClasificacionRegla.DEL_PLAN, EstadoRegla.VIGENTE);

    Validacion validacion =
        ValidadorDelConsolidado.validar(PROGRAMA, Optional.empty(), TEMPORAL, List.of(nueva));

    assertThat(validacion).isInstanceOf(Validacion.Invalido.class);
    assertThat(((Validacion.Invalido) validacion).motivos())
        .containsExactly(
            "La regla \"Máximo de créditos\" (art. 2) es temporal y está clasificada como "
                + "DEL_PLAN; debe ser INFORMATIVA.");
  }

  @Test
  void uc07_ext6f_unaReglaInformativaDeUnaNormaTemporalPasaElValidador() {
    ReglaEnVersion nueva =
        enVersion(
            "Máximo de créditos", "art. 2", ClasificacionRegla.INFORMATIVA, EstadoRegla.VIGENTE);

    Validacion validacion =
        ValidadorDelConsolidado.validar(PROGRAMA, Optional.empty(), TEMPORAL, List.of(nueva));

    assertThat(validacion).isInstanceOf(Validacion.Valido.class);
    assertThat(((Validacion.Valido) validacion).consolidado().reglas()).containsExactly(nueva);
  }

  @Test
  void uc07_ext6f_elValidadorSumaLasReglasDeLaVersionAnterior() {
    ReglaEnVersion anterior =
        enVersion(
            "Créditos del plan de estudios",
            "art. 1",
            ClasificacionRegla.DEL_PLAN,
            EstadoRegla.YA_NO_RIGE);
    ReglaEnVersion nueva =
        enVersion(
            "Componentes del programa",
            "art. 2",
            ClasificacionRegla.INFORMATIVA,
            EstadoRegla.VIGENTE);

    Validacion validacion =
        ValidadorDelConsolidado.validar(
            PROGRAMA, Optional.of(versionAnterior(anterior)), PERMANENTE, List.of(nueva));

    assertThat(validacion).isInstanceOf(Validacion.Valido.class);
    assertThat(((Validacion.Valido) validacion).consolidado().reglas())
        .containsExactly(anterior, nueva);
  }

  @Test
  void tt02_elValidadorNoRevisaDeNuevoLaVersionAnterior() {
    ReglaEnVersion anterior =
        enVersion(
            "Créditos del plan de estudios",
            "art. 1",
            ClasificacionRegla.DEL_PLAN,
            EstadoRegla.VIGENTE);
    ReglaEnVersion nueva =
        enVersion(
            "Máximo de créditos", "art. 2", ClasificacionRegla.INFORMATIVA, EstadoRegla.VIGENTE);

    Validacion validacion =
        ValidadorDelConsolidado.validar(
            PROGRAMA, Optional.of(versionAnterior(anterior)), TEMPORAL, List.of(nueva));

    assertThat(validacion).isInstanceOf(Validacion.Valido.class);
  }

  @Test
  void uc07_hu03_t4_laUnionConservaElOrdenDeLaVersionAnteriorYLasNuevas() {
    ReglaEnVersion anterior =
        enVersion(
            "Créditos del programa", "art. 1", ClasificacionRegla.INFORMATIVA, EstadoRegla.VIGENTE);
    ReglaEnVersion nueva =
        enVersion(
            "Componentes del programa",
            "art. 1",
            ClasificacionRegla.INFORMATIVA,
            EstadoRegla.VIGENTE);

    Validacion validacion =
        ValidadorDelConsolidado.validar(
            PROGRAMA, Optional.of(versionAnterior(anterior)), PERMANENTE, List.of(nueva));

    assertThat(((Validacion.Valido) validacion).consolidado().reglas())
        .containsExactly(anterior, nueva);
  }

  @Test
  void uc07_hu03_t4_laUnionConservaElEstadoDeLasReglasDeLaVersionAnterior() {
    ReglaEnVersion anterior =
        enVersion(
            "Créditos del programa",
            "art. 1",
            ClasificacionRegla.INFORMATIVA,
            EstadoRegla.YA_NO_RIGE);
    ReglaEnVersion nueva =
        enVersion(
            "Componentes del programa",
            "art. 1",
            ClasificacionRegla.INFORMATIVA,
            EstadoRegla.VIGENTE);

    Validacion validacion =
        ValidadorDelConsolidado.validar(
            PROGRAMA, Optional.of(versionAnterior(anterior)), PERMANENTE, List.of(nueva));

    assertThat(((Validacion.Valido) validacion).consolidado().reglas())
        .containsExactly(anterior, nueva);
  }

  @Test
  void uc07_hu03_t4_sinVersionAnteriorLaUnionSoloTieneLasNuevas() {
    ReglaEnVersion nueva =
        enVersion(
            "Componentes del programa",
            "art. 1",
            ClasificacionRegla.INFORMATIVA,
            EstadoRegla.VIGENTE);

    Validacion validacion =
        ValidadorDelConsolidado.validar(PROGRAMA, Optional.empty(), PERMANENTE, List.of(nueva));

    assertThat(((Validacion.Valido) validacion).consolidado().reglas()).containsExactly(nueva);
  }

  @Test
  void uc07_hu03_t4_unReglaIdDeLaVersionAnteriorSeRechaza() {
    Regla regla = regla("Créditos del programa", "art. 1", ClasificacionRegla.INFORMATIVA);
    ReglaEnVersion anterior = new ReglaEnVersion(regla, EstadoRegla.PROGRAMADA);
    ReglaEnVersion nueva = new ReglaEnVersion(regla, EstadoRegla.VIGENTE);

    Validacion validacion =
        ValidadorDelConsolidado.validar(
            PROGRAMA, Optional.of(versionAnterior(anterior)), PERMANENTE, List.of(nueva));

    assertThat(validacion).isInstanceOf(Validacion.Invalido.class);
    assertThat(((Validacion.Invalido) validacion).motivos())
        .anyMatch(
            motivo ->
                motivo.contains("Créditos del programa")
                    && motivo.contains("art. 1")
                    && motivo.contains("ya está en la versión anterior del consolidado."));
  }

  @Test
  void uc07_hu03_t4_unReglaIdRepetidoEntreLasNuevasSeRechaza() {
    ReglaEnVersion nueva =
        enVersion(
            "Componentes del programa",
            "art. 1",
            ClasificacionRegla.INFORMATIVA,
            EstadoRegla.VIGENTE);

    Validacion validacion =
        ValidadorDelConsolidado.validar(
            PROGRAMA, Optional.empty(), PERMANENTE, List.of(nueva, nueva));

    assertThat(validacion).isInstanceOf(Validacion.Invalido.class);
    assertThat(((Validacion.Invalido) validacion).motivos())
        .anyMatch(
            motivo ->
                motivo.contains("Componentes del programa")
                    && motivo.contains("está repetida en la norma que se importa."));
  }

  @Test
  void tt02_elValidadorRechazaUnaVersionAnteriorDeOtroPrograma() {
    VersionDelConsolidado anteriorDeOtro =
        VersionDelConsolidado.primera(
            new ConsolidadoValidado(ProgramaId.nuevo(), List.of()), MOTIVO, FECHA);

    assertThatThrownBy(
            () ->
                ValidadorDelConsolidado.validar(
                    PROGRAMA, Optional.of(anteriorDeOtro), PERMANENTE, List.of()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("la versión anterior es de otro programa");
  }

  @Test
  void tt02_unResultadoInvalidoExigeAlMenosUnMotivo() {
    assertThatThrownBy(() -> new Validacion.Invalido(List.of()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("motivos no puede ser vacío");
  }

  private static VersionDelConsolidado versionAnterior(ReglaEnVersion... reglas) {
    return VersionDelConsolidado.primera(
        new ConsolidadoValidado(PROGRAMA, List.of(reglas)), MOTIVO, FECHA);
  }

  private static ReglaEnVersion enVersion(
      String objeto, String articulo, ClasificacionRegla clasificacion, EstadoRegla estado) {
    return new ReglaEnVersion(regla(objeto, articulo, clasificacion), estado);
  }

  private static Regla regla(String objeto, String articulo, ClasificacionRegla clasificacion) {
    return Regla.registrar(
        objeto,
        "Enunciado de " + objeto,
        new Fuente(NORMA, articulo),
        Optional.empty(),
        clasificacion,
        Optional.empty(),
        new DatosDeRegla.DeOtraCategoria());
  }
}
