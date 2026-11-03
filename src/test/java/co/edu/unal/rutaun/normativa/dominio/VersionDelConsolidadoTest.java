package co.edu.unal.rutaun.normativa.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.rutaun.compartido.Creditos;
import co.edu.unal.rutaun.compartido.EstadoRegla;
import co.edu.unal.rutaun.compartido.NormaId;
import co.edu.unal.rutaun.compartido.ProgramaId;
import co.edu.unal.rutaun.compartido.ReglaId;
import co.edu.unal.rutaun.normativa.ClasificacionRegla;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class VersionDelConsolidadoTest {

  private static final ProgramaId PROGRAMA = ProgramaId.nuevo();
  private static final NormaId NORMA = NormaId.nuevo();
  private static final LocalDate FECHA = LocalDate.of(2026, 10, 1);
  private static final MotivoDeVersion MOTIVO = new MotivoDeVersion.PorIncorporacion(NORMA, "Ana");
  private static final MotivoDeVersion CORRECCION = new MotivoDeVersion.PorCorreccion("Ana");

  private static Regla estructural(ReglaId id, EstadoRegla estado) {
    return regla(id, estado, new DatosDeRegla.DePrograma(new Creditos(160)));
  }

  private static Regla noEstructural(ReglaId id, EstadoRegla estado) {
    return regla(id, estado, new DatosDeRegla.DeOtraCategoria());
  }

  private static Regla regla(ReglaId id, EstadoRegla estado, DatosDeRegla datos) {
    return Regla.reconstituir(
        id,
        "Objeto",
        "Enunciado",
        new Fuente(NORMA, "art. 1"),
        Optional.empty(),
        ClasificacionRegla.DEL_PLAN,
        estado,
        Optional.empty(),
        datos);
  }

  private static ConsolidadoValidado validado(Regla... reglas) {
    return new ConsolidadoValidado(PROGRAMA, List.of(reglas));
  }

  @Test
  void tt02_primeraVersionEmpiezaEnUnoYGuardaSusDatos() {
    Regla regla = estructural(ReglaId.nuevo(), EstadoRegla.VIGENTE);

    VersionDelConsolidado primera = VersionDelConsolidado.primera(validado(regla), MOTIVO, FECHA);

    assertThat(primera.programa()).isEqualTo(PROGRAMA);
    assertThat(primera.numero()).isEqualTo(1);
    assertThat(primera.fecha()).isEqualTo(FECHA);
    assertThat(primera.motivo()).isEqualTo(MOTIVO);
    assertThat(primera.reglas()).containsExactly(regla);
    assertThat(primera.versionDelPlan()).isEqualTo(1);
  }

  @Test
  void tt02_siguienteCreaLaVersionConElNumeroSiguienteYNoTocaLaAnterior() {
    Regla vieja = estructural(ReglaId.nuevo(), EstadoRegla.VIGENTE);
    VersionDelConsolidado primera = VersionDelConsolidado.primera(validado(vieja), MOTIVO, FECHA);
    LocalDate despues = FECHA.plusDays(30);

    VersionDelConsolidado segunda = primera.siguiente(validado(vieja), CORRECCION, despues);

    assertThat(segunda.numero()).isEqualTo(2);
    assertThat(segunda.programa()).isEqualTo(PROGRAMA);
    assertThat(segunda.fecha()).isEqualTo(despues);
    assertThat(segunda.motivo()).isEqualTo(CORRECCION);
    assertThat(primera.numero()).isEqualTo(1);
    assertThat(primera.fecha()).isEqualTo(FECHA);
    assertThat(primera.motivo()).isEqualTo(MOTIVO);
    assertThat(primera.reglas()).containsExactly(vieja);
  }

  @Test
  void tt02_laVersionDelPlanNoSubeSiSoloCambianReglasQueNoSonDeEstructura() {
    Regla plan = estructural(ReglaId.nuevo(), EstadoRegla.VIGENTE);
    Regla carga = noEstructural(ReglaId.nuevo(), EstadoRegla.VIGENTE);
    VersionDelConsolidado primera = VersionDelConsolidado.primera(validado(plan), MOTIVO, FECHA);

    VersionDelConsolidado segunda = primera.siguiente(validado(plan, carga), MOTIVO, FECHA);

    assertThat(segunda.numero()).isEqualTo(2);
    assertThat(segunda.versionDelPlan()).isEqualTo(1);
  }

  @Test
  void tt02_laVersionDelPlanSubeSiSeAgregaUnaReglaDeEstructura() {
    Regla plan = estructural(ReglaId.nuevo(), EstadoRegla.VIGENTE);
    Regla otra = estructural(ReglaId.nuevo(), EstadoRegla.VIGENTE);
    VersionDelConsolidado primera = VersionDelConsolidado.primera(validado(plan), MOTIVO, FECHA);

    VersionDelConsolidado segunda = primera.siguiente(validado(plan, otra), MOTIVO, FECHA);

    assertThat(segunda.versionDelPlan()).isEqualTo(2);
    assertThat(primera.versionDelPlan()).isEqualTo(1);
  }

  @Test
  void tt02_laVersionDelPlanSubeSiUnaReglaDeEstructuraCambiaDeEstado() {
    ReglaId id = ReglaId.nuevo();
    VersionDelConsolidado primera =
        VersionDelConsolidado.primera(
            validado(estructural(id, EstadoRegla.PROGRAMADA)), MOTIVO, FECHA);

    VersionDelConsolidado segunda =
        primera.siguiente(validado(estructural(id, EstadoRegla.VIGENTE)), MOTIVO, FECHA);

    assertThat(segunda.versionDelPlan()).isEqualTo(2);
  }

  @Test
  void tt02_laVersionDelPlanSubeSiSeRetiraUnaReglaDeEstructura() {
    Regla plan = estructural(ReglaId.nuevo(), EstadoRegla.VIGENTE);
    VersionDelConsolidado primera = VersionDelConsolidado.primera(validado(plan), MOTIVO, FECHA);

    VersionDelConsolidado segunda = primera.siguiente(validado(), MOTIVO, FECHA);

    assertThat(segunda.versionDelPlan()).isEqualTo(2);
  }

  @Test
  void tt02_laVersionDelPlanSeConservaEnLasVersionesSiguientes() {
    Regla plan = estructural(ReglaId.nuevo(), EstadoRegla.VIGENTE);
    Regla otra = estructural(ReglaId.nuevo(), EstadoRegla.VIGENTE);
    VersionDelConsolidado v1 = VersionDelConsolidado.primera(validado(plan), MOTIVO, FECHA);
    VersionDelConsolidado v2 = v1.siguiente(validado(plan, otra), MOTIVO, FECHA);

    VersionDelConsolidado v3 = v2.siguiente(validado(plan, otra), CORRECCION, FECHA);

    assertThat(v3.numero()).isEqualTo(3);
    assertThat(v3.versionDelPlan()).isEqualTo(2);
  }

  @Test
  void tt02_siguienteRechazaReglasValidadasDeOtroPrograma() {
    VersionDelConsolidado primera = VersionDelConsolidado.primera(validado(), MOTIVO, FECHA);
    ConsolidadoValidado deOtro = new ConsolidadoValidado(ProgramaId.nuevo(), List.of());

    assertThatThrownBy(() -> primera.siguiente(deOtro, MOTIVO, FECHA))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("las reglas validadas son de otro programa");
  }

  @Test
  void tt02_laVersionVigenteEsLaDeMayorNumero() {
    VersionDelConsolidado v1 = VersionDelConsolidado.primera(validado(), MOTIVO, FECHA);
    VersionDelConsolidado v2 = v1.siguiente(validado(), MOTIVO, FECHA);
    VersionDelConsolidado v3 = v2.siguiente(validado(), MOTIVO, FECHA);

    assertThat(VersionDelConsolidado.vigenteEntre(List.of(v2, v3, v1))).contains(v3);
    assertThat(VersionDelConsolidado.vigenteEntre(List.of())).isEmpty();
  }

  @Test
  void tt02_laVersionNoSeAfectaPorCambiosEnLaListaDeReglasQueRecibio() {
    Regla regla = estructural(ReglaId.nuevo(), EstadoRegla.VIGENTE);
    List<Regla> reglas = new ArrayList<>(List.of(regla));
    VersionDelConsolidado version =
        VersionDelConsolidado.reconstituir(PROGRAMA, 1, FECHA, MOTIVO, reglas, 1);

    reglas.clear();

    assertThat(version.reglas()).containsExactly(regla);
  }

  @Test
  void tt02_laVersionNoDejaModificarSusReglas() {
    VersionDelConsolidado version =
        VersionDelConsolidado.primera(
            validado(estructural(ReglaId.nuevo(), EstadoRegla.VIGENTE)), MOTIVO, FECHA);

    assertThatThrownBy(() -> version.reglas().clear())
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void tt02_reconstituirRechazaDatosInvalidos() {
    List<Regla> sinReglas = List.of();

    assertThatThrownBy(
            () -> VersionDelConsolidado.reconstituir(PROGRAMA, 0, FECHA, MOTIVO, sinReglas, 1))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("numero debe ser mayor que cero");
    assertThatThrownBy(
            () -> VersionDelConsolidado.reconstituir(PROGRAMA, 2, FECHA, MOTIVO, sinReglas, 0))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("versionDelPlan debe ser mayor que cero");
    assertThatThrownBy(
            () -> VersionDelConsolidado.reconstituir(PROGRAMA, 1, FECHA, MOTIVO, sinReglas, 2))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("la versión del plan no puede superar el número de la versión");
    assertThatThrownBy(
            () -> VersionDelConsolidado.reconstituir(null, 1, FECHA, MOTIVO, sinReglas, 1))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("programa no puede ser null");
    assertThatThrownBy(
            () -> VersionDelConsolidado.reconstituir(PROGRAMA, 1, null, MOTIVO, sinReglas, 1))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("fecha no puede ser null");
    assertThatThrownBy(
            () -> VersionDelConsolidado.reconstituir(PROGRAMA, 1, FECHA, null, sinReglas, 1))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("motivo no puede ser null");
  }

  @Test
  void tt02_dosVersionesSonLaMismaSiTienenElMismoProgramaYNumero() {
    VersionDelConsolidado una = VersionDelConsolidado.primera(validado(), MOTIVO, FECHA);
    VersionDelConsolidado otra = VersionDelConsolidado.primera(validado(), CORRECCION, FECHA);

    assertThat(una).isEqualTo(otra);
    assertThat(una.siguiente(validado(), MOTIVO, FECHA)).isNotEqualTo(una);
  }
}
