package co.edu.unal.rutaun.normativa.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.rutaun.compartido.Momento;
import co.edu.unal.rutaun.compartido.NormaId;
import co.edu.unal.rutaun.compartido.ParteDelAnio;
import co.edu.unal.rutaun.compartido.PeriodoAcademico;
import co.edu.unal.rutaun.normativa.TipoAlcance;
import co.edu.unal.rutaun.normativa.TipoDeEfecto;
import co.edu.unal.rutaun.normativa.TipoNorma;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

class NormaTest {

  private static final Facultad INGENIERIA = new Facultad("Ingeniería");
  private static final Programa SISTEMAS =
      Programa.registrar("Ingeniería de Sistemas", "2879", INGENIERIA);
  private static final Programa CIVIL = Programa.registrar("Ingeniería Civil", "2880", INGENIERIA);
  private static final IdentificacionNorma IDENTIFICACION =
      new IdentificacionNorma(TipoNorma.ACUERDO, "003-A", 2022, "Consejo Académico");
  private static final Vigencia VIGENCIA =
      new Vigencia(
          new Momento.EnPeriodo(new PeriodoAcademico(2022, ParteDelAnio.SEGUNDO)),
          Optional.empty());
  private static final Alcance ALCANCE =
      new Alcance(
          TipoAlcance.PROGRAMAS,
          Set.of(),
          Set.of(SISTEMAS.id()),
          Set.of(),
          "El programa de Ingeniería de Sistemas");
  private static final LocalDate EXPEDICION = LocalDate.of(2022, 8, 1);

  @Test
  void tt03_normaRegistrarGuardaIdentificacionVigenciaYAlcance() {
    Norma norma =
        Norma.registrar(
            IDENTIFICACION,
            TipoDeEfecto.BASE,
            EXPEDICION,
            VIGENCIA,
            Optional.of("https://regimenlegal.unal.edu.co/acuerdo-003a-2022"),
            ALCANCE);

    assertThat(norma.identificacion()).isEqualTo(IDENTIFICACION);
    assertThat(norma.tipoDeEfecto()).isEqualTo(TipoDeEfecto.BASE);
    assertThat(norma.fechaExpedicion()).isEqualTo(EXPEDICION);
    assertThat(norma.vigencia()).isEqualTo(VIGENCIA);
    assertThat(norma.enlaceRegimenLegal())
        .contains("https://regimenlegal.unal.edu.co/acuerdo-003a-2022");
    assertThat(norma.alcance()).isEqualTo(ALCANCE);
  }

  @Test
  void tt03_normaRegistrarGeneraUnIdentificadorNuevoCadaVez() {
    Norma una = registrarBase();
    Norma otra = registrarBase();

    assertThat(una.id()).isNotEqualTo(otra.id());
    assertThat(una).isNotEqualTo(otra);
  }

  @Test
  void tt03_normaReconstituirConservaElIdentificador() {
    NormaId id = NormaId.nuevo();

    Norma norma = reconstituirBase(id);

    assertThat(norma.id()).isEqualTo(id);
    assertThat(norma.identificacion()).isEqualTo(IDENTIFICACION);
  }

  @Test
  void tt03_normaRechazaDatosObligatoriosNulos() {
    assertThatThrownBy(
            () ->
                Norma.registrar(
                    null, TipoDeEfecto.BASE, EXPEDICION, VIGENCIA, Optional.empty(), ALCANCE))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("identificacion no puede ser null");
    assertThatThrownBy(
            () ->
                Norma.registrar(
                    IDENTIFICACION, null, EXPEDICION, VIGENCIA, Optional.empty(), ALCANCE))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("tipoDeEfecto no puede ser null");
    assertThatThrownBy(
            () ->
                Norma.registrar(
                    IDENTIFICACION, TipoDeEfecto.BASE, null, VIGENCIA, Optional.empty(), ALCANCE))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("fechaExpedicion no puede ser null");
    assertThatThrownBy(
            () ->
                Norma.registrar(
                    IDENTIFICACION, TipoDeEfecto.BASE, EXPEDICION, null, Optional.empty(), ALCANCE))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("vigencia no puede ser null");
    assertThatThrownBy(
            () ->
                Norma.registrar(
                    IDENTIFICACION, TipoDeEfecto.BASE, EXPEDICION, VIGENCIA, null, ALCANCE))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("enlaceRegimenLegal no puede ser null");
    assertThatThrownBy(
            () ->
                Norma.registrar(
                    IDENTIFICACION,
                    TipoDeEfecto.BASE,
                    EXPEDICION,
                    VIGENCIA,
                    Optional.empty(),
                    null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("alcance no puede ser null");
  }

  @Test
  void tt03_normaExcepcionSinVigenciaHastaEsRechazada() {
    assertThatThrownBy(
            () ->
                Norma.registrar(
                    IDENTIFICACION,
                    TipoDeEfecto.EXCEPCION_O_MEDIDA_TEMPORAL,
                    EXPEDICION,
                    VIGENCIA,
                    Optional.empty(),
                    ALCANCE))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("una excepción o medida temporal debe tener vigencia hasta");
  }

  @Test
  void tt03_normaExcepcionConVigenciaHastaSeAcepta() {
    Vigencia temporal =
        new Vigencia(
            new Momento.EnPeriodo(new PeriodoAcademico(2026, ParteDelAnio.SEGUNDO)),
            Optional.of(new Momento.EnPeriodo(new PeriodoAcademico(2026, ParteDelAnio.SEGUNDO))));

    Norma norma =
        Norma.registrar(
            IDENTIFICACION,
            TipoDeEfecto.EXCEPCION_O_MEDIDA_TEMPORAL,
            EXPEDICION,
            temporal,
            Optional.empty(),
            ALCANCE);

    assertThat(norma.vigencia().esTemporal()).isTrue();
  }

  @Test
  void tt03_normaEnlaceEsOpcionalPeroNoVacio() {
    Norma sinEnlace =
        Norma.registrar(
            IDENTIFICACION, TipoDeEfecto.BASE, EXPEDICION, VIGENCIA, Optional.empty(), ALCANCE);

    assertThat(sinEnlace.enlaceRegimenLegal()).isEmpty();
    assertThatThrownBy(
            () ->
                Norma.registrar(
                    IDENTIFICACION,
                    TipoDeEfecto.BASE,
                    EXPEDICION,
                    VIGENCIA,
                    Optional.of(" "),
                    ALCANCE))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("enlaceRegimenLegal no puede ser vacío");
  }

  @Test
  void tt03_normaConElMismoIdentificadorEsLaMismaNorma() {
    NormaId id = NormaId.nuevo();

    Norma una = reconstituirBase(id);
    Norma otra = reconstituirBase(id);

    assertThat(una).isEqualTo(otra).hasSameHashCodeAs(otra);
  }

  @Test
  void tt03_normaConAlcancePorProgramasComprendeSoloElProgramaElegido() {
    Norma norma = registrarBase();

    assertThat(norma.alcance().comprende(SISTEMAS)).isTrue();
    assertThat(norma.alcance().comprende(CIVIL)).isFalse();
  }

  private static Norma registrarBase() {
    return Norma.registrar(
        IDENTIFICACION, TipoDeEfecto.BASE, EXPEDICION, VIGENCIA, Optional.empty(), ALCANCE);
  }

  private static Norma reconstituirBase(NormaId id) {
    return Norma.reconstituir(
        id, IDENTIFICACION, TipoDeEfecto.BASE, EXPEDICION, VIGENCIA, Optional.empty(), ALCANCE);
  }
}
