package co.edu.unal.rutaun.normativa.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.compartido.Creditos;
import co.edu.unal.rutaun.compartido.EstadoRegla;
import co.edu.unal.rutaun.compartido.NormaId;
import co.edu.unal.rutaun.compartido.PeriodoAcademico;
import co.edu.unal.rutaun.compartido.ProgramaId;
import co.edu.unal.rutaun.compartido.ReglaId;
import co.edu.unal.rutaun.normativa.ClasificacionRegla;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

class VersionDelConsolidadoTest {

  private static final ProgramaId PROGRAMA = ProgramaId.nuevo();
  private static final NormaId NORMA = NormaId.nuevo();
  private static final LocalDate FECHA = LocalDate.of(2026, 10, 1);
  private static final MotivoDeVersion MOTIVO = new MotivoDeVersion.PorIncorporacion(NORMA, "Ana");
  private static final MotivoDeVersion CORRECCION = new MotivoDeVersion.PorCorreccion("Ana");

  private static ReglaEnVersion estructural(ReglaId id, EstadoRegla estado) {
    return new ReglaEnVersion(regla(id, new DatosDeRegla.DePrograma(new Creditos(160))), estado);
  }

  private static ReglaEnVersion noEstructural(ReglaId id, EstadoRegla estado) {
    return new ReglaEnVersion(regla(id, new DatosDeRegla.DeOtraCategoria()), estado);
  }

  private static Regla regla(ReglaId id, DatosDeRegla datos) {
    return Regla.reconstituir(
        id,
        "Objeto",
        "Enunciado",
        new Fuente(NORMA, "art. 1"),
        Optional.empty(),
        ClasificacionRegla.DEL_PLAN,
        Optional.empty(),
        datos);
  }

  private static ConsolidadoValidado validado(ReglaEnVersion... reglas) {
    return new ConsolidadoValidado(PROGRAMA, List.of(reglas));
  }

  @Test
  void tt02_primeraVersionEmpiezaEnUnoYGuardaSusDatos() {
    ReglaEnVersion regla = estructural(ReglaId.nuevo(), EstadoRegla.VIGENTE);

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
    ReglaEnVersion vieja = estructural(ReglaId.nuevo(), EstadoRegla.VIGENTE);
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
    ReglaEnVersion plan = estructural(ReglaId.nuevo(), EstadoRegla.VIGENTE);
    ReglaEnVersion carga = noEstructural(ReglaId.nuevo(), EstadoRegla.VIGENTE);
    VersionDelConsolidado primera = VersionDelConsolidado.primera(validado(plan), MOTIVO, FECHA);

    VersionDelConsolidado segunda = primera.siguiente(validado(plan, carga), MOTIVO, FECHA);

    assertThat(segunda.numero()).isEqualTo(2);
    assertThat(segunda.versionDelPlan()).isEqualTo(1);
  }

  @Test
  void tt02_laVersionDelPlanSubeSiSeAgregaUnaReglaDeEstructura() {
    ReglaEnVersion plan = estructural(ReglaId.nuevo(), EstadoRegla.VIGENTE);
    ReglaEnVersion otra = estructural(ReglaId.nuevo(), EstadoRegla.VIGENTE);
    VersionDelConsolidado primera = VersionDelConsolidado.primera(validado(plan), MOTIVO, FECHA);

    VersionDelConsolidado segunda = primera.siguiente(validado(plan, otra), MOTIVO, FECHA);

    assertThat(segunda.versionDelPlan()).isEqualTo(2);
    assertThat(primera.versionDelPlan()).isEqualTo(1);
  }

  @Test
  void tt02_laVersionDelPlanNoSubeSiUnaReglaDeEstructuraSoloCambiaDeEstado() {
    Regla regla = regla(ReglaId.nuevo(), new DatosDeRegla.DePrograma(new Creditos(160)));
    VersionDelConsolidado primera =
        VersionDelConsolidado.primera(
            validado(new ReglaEnVersion(regla, EstadoRegla.PROGRAMADA)), MOTIVO, FECHA);

    VersionDelConsolidado segunda =
        primera.siguiente(validado(new ReglaEnVersion(regla, EstadoRegla.VIGENTE)), MOTIVO, FECHA);

    assertThat(segunda.numero()).isEqualTo(2);
    assertThat(segunda.versionDelPlan()).isEqualTo(1);
  }

  @Test
  void s19_unaVersionAutomaticaNoSubeLaVersionDelPlan() {
    ReglaEnVersion base = estructural(ReglaId.nuevo(), EstadoRegla.VIGENTE);
    Regla reforma = regla(ReglaId.nuevo(), new DatosDeRegla.DePrograma(new Creditos(160)));
    ReglaEnVersion otra = estructural(ReglaId.nuevo(), EstadoRegla.VIGENTE);
    VersionDelConsolidado v1 = VersionDelConsolidado.primera(validado(base), MOTIVO, FECHA);

    VersionDelConsolidado v2 =
        v1.siguiente(
            validado(base, new ReglaEnVersion(reforma, EstadoRegla.PROGRAMADA)), MOTIVO, FECHA);
    VersionDelConsolidado v3 =
        v2.siguiente(
            validado(base, new ReglaEnVersion(reforma, EstadoRegla.VIGENTE)),
            new MotivoDeVersion.AutomaticoPorFecha(FECHA),
            FECHA);
    VersionDelConsolidado v4 =
        v3.siguiente(
            validado(base, new ReglaEnVersion(reforma, EstadoRegla.VIGENTE), otra),
            new MotivoDeVersion.AutomaticoPorPeriodo(PeriodoAcademico.desdeTexto("2027-1")),
            FECHA);
    VersionDelConsolidado v5 =
        v4.siguiente(
            validado(base, new ReglaEnVersion(reforma, EstadoRegla.VIGENTE)),
            new MotivoDeVersion.AutomaticoPorFecha(FECHA),
            FECHA);

    assertThat(v1.versionDelPlan()).isEqualTo(1);
    assertThat(v2.versionDelPlan()).isEqualTo(2);
    assertThat(v3.versionDelPlan()).isEqualTo(2);
    assertThat(v4.versionDelPlan()).isEqualTo(2);
    assertThat(v5.versionDelPlan()).isEqualTo(2);
    assertThat(v5.numero()).isEqualTo(5);
  }

  @Test
  void s19_unaCorreccionNoSubeLaVersionDelPlan() {
    ReglaEnVersion base = estructural(ReglaId.nuevo(), EstadoRegla.VIGENTE);
    ReglaEnVersion reemplazo = estructural(ReglaId.nuevo(), EstadoRegla.VIGENTE);
    VersionDelConsolidado v1 = VersionDelConsolidado.primera(validado(base), MOTIVO, FECHA);

    VersionDelConsolidado v2 = v1.siguiente(validado(reemplazo), CORRECCION, FECHA);

    assertThat(v2.versionDelPlan()).isEqualTo(1);
  }

  @Test
  void s19_unaReformaProgramadaSubeLaVersionDelPlanAlIncorporarse() {
    ReglaEnVersion base = estructural(ReglaId.nuevo(), EstadoRegla.VIGENTE);
    ReglaEnVersion reforma = estructural(ReglaId.nuevo(), EstadoRegla.PROGRAMADA);
    VersionDelConsolidado v1 = VersionDelConsolidado.primera(validado(base), MOTIVO, FECHA);

    VersionDelConsolidado v2 = v1.siguiente(validado(base, reforma), MOTIVO, FECHA);

    assertThat(v2.versionDelPlan()).isEqualTo(2);
  }

  @Test
  void s19_unaEquivalenciaNuevaNoSubeLaVersionDelPlan() {
    ReglaEnVersion base = estructural(ReglaId.nuevo(), EstadoRegla.VIGENTE);
    ReglaEnVersion equivalencia =
        new ReglaEnVersion(
            regla(
                ReglaId.nuevo(),
                new DatosDeRegla.DeEquivalencia(
                    Set.of(new CodigoMateria("MAT-1")), new CodigoMateria("MAT-2"))),
            EstadoRegla.VIGENTE);
    VersionDelConsolidado v1 = VersionDelConsolidado.primera(validado(base), MOTIVO, FECHA);

    VersionDelConsolidado v2 = v1.siguiente(validado(base, equivalencia), MOTIVO, FECHA);

    assertThat(v2.versionDelPlan()).isEqualTo(1);
  }

  @Test
  void tt02_siguienteRechazaMotivoYFechaNull() {
    VersionDelConsolidado primera = VersionDelConsolidado.primera(validado(), MOTIVO, FECHA);

    assertThatThrownBy(() -> primera.siguiente(validado(), null, FECHA))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("motivo no puede ser null");
    assertThatThrownBy(() -> primera.siguiente(validado(), MOTIVO, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("fecha no puede ser null");
  }

  @Test
  void tt02_laVersionDelPlanSubeSiSeRetiraUnaReglaDeEstructura() {
    ReglaEnVersion plan = estructural(ReglaId.nuevo(), EstadoRegla.VIGENTE);
    VersionDelConsolidado primera = VersionDelConsolidado.primera(validado(plan), MOTIVO, FECHA);

    VersionDelConsolidado segunda = primera.siguiente(validado(), MOTIVO, FECHA);

    assertThat(segunda.versionDelPlan()).isEqualTo(2);
  }

  @Test
  void tt02_laVersionDelPlanSeConservaEnLasVersionesSiguientes() {
    ReglaEnVersion plan = estructural(ReglaId.nuevo(), EstadoRegla.VIGENTE);
    ReglaEnVersion otra = estructural(ReglaId.nuevo(), EstadoRegla.VIGENTE);
    VersionDelConsolidado v1 = VersionDelConsolidado.primera(validado(plan), MOTIVO, FECHA);
    VersionDelConsolidado v2 = v1.siguiente(validado(plan, otra), MOTIVO, FECHA);

    VersionDelConsolidado v3 = v2.siguiente(validado(plan, otra), CORRECCION, FECHA);

    assertThat(v3.numero()).isEqualTo(3);
    assertThat(v3.versionDelPlan()).isEqualTo(2);
  }

  @Test
  void tt02_laMismaReglaPuedeTenerEstadosDistintosEnDosVersiones() {
    Regla regla = regla(ReglaId.nuevo(), new DatosDeRegla.DePrograma(new Creditos(160)));
    VersionDelConsolidado v1 =
        VersionDelConsolidado.primera(
            validado(new ReglaEnVersion(regla, EstadoRegla.PROGRAMADA)), MOTIVO, FECHA);

    VersionDelConsolidado v2 =
        v1.siguiente(
            validado(new ReglaEnVersion(regla, EstadoRegla.VIGENTE)),
            new MotivoDeVersion.AutomaticoPorFecha(FECHA),
            FECHA);

    assertThat(v1.reglas()).containsExactly(new ReglaEnVersion(regla, EstadoRegla.PROGRAMADA));
    assertThat(v2.reglas()).containsExactly(new ReglaEnVersion(regla, EstadoRegla.VIGENTE));
    assertThat(v2.reglas().get(0).regla()).isSameAs(v1.reglas().get(0).regla());
    assertThat(v1.versionDelPlan()).isEqualTo(1);
    assertThat(v2.versionDelPlan()).isEqualTo(1);
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
    ReglaEnVersion regla = estructural(ReglaId.nuevo(), EstadoRegla.VIGENTE);
    List<ReglaEnVersion> reglas = new ArrayList<>(List.of(regla));
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
    List<ReglaEnVersion> sinReglas = List.of();

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
