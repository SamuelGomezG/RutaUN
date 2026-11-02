package co.edu.unal.rutaun.normativa.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.rutaun.compartido.ProgramaId;
import co.edu.unal.rutaun.normativa.TipoAlcance;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class AlcanceTest {

  private static final Facultad INGENIERIA = new Facultad("Ingeniería");
  private static final Facultad MEDICINA = new Facultad("Medicina");
  private static final Programa SISTEMAS =
      Programa.registrar("Ingeniería de Sistemas", "2879", INGENIERIA);
  private static final Programa CIVIL = Programa.registrar("Ingeniería Civil", "2880", INGENIERIA);
  private static final Programa MEDICINA_PROGRAMA =
      Programa.registrar("Medicina", "2501", MEDICINA);

  @Test
  void tt03_alcanceDeTodosComprendeCualquierPrograma() {
    Alcance alcance =
        new Alcance(
            TipoAlcance.TODOS, Set.of(), Set.of(), Set.of(), "Todos los programas de pregrado");

    assertThat(alcance.comprende(SISTEMAS)).isTrue();
    assertThat(alcance.comprende(MEDICINA_PROGRAMA)).isTrue();
  }

  @Test
  void tt03_alcancePorFacultadesComprendeSoloLosProgramasDeEsasFacultades() {
    Alcance alcance =
        new Alcance(
            TipoAlcance.FACULTADES,
            Set.of(INGENIERIA),
            Set.of(),
            Set.of(),
            "Los programas de la Facultad de Ingeniería");

    assertThat(alcance.comprende(SISTEMAS)).isTrue();
    assertThat(alcance.comprende(CIVIL)).isTrue();
    assertThat(alcance.comprende(MEDICINA_PROGRAMA)).isFalse();
  }

  @Test
  void tt03_alcancePorProgramasComprendeSoloLosElegidos() {
    Alcance alcance =
        new Alcance(
            TipoAlcance.PROGRAMAS,
            Set.of(),
            Set.of(SISTEMAS.id()),
            Set.of(),
            "El programa de Ingeniería de Sistemas");

    assertThat(alcance.comprende(SISTEMAS)).isTrue();
    assertThat(alcance.comprende(CIVIL)).isFalse();
  }

  @Test
  void tt03_alcanceExcluyeLosProgramasExcluidos() {
    Alcance alcance =
        new Alcance(
            TipoAlcance.TODOS,
            Set.of(),
            Set.of(),
            Set.of(MEDICINA_PROGRAMA.id()),
            "Todos los programas, excepto Medicina");

    assertThat(alcance.comprende(SISTEMAS)).isTrue();
    assertThat(alcance.comprende(MEDICINA_PROGRAMA)).isFalse();
  }

  @Test
  void tt03_alcanceRechazaCombinacionesIncoherentesConElTipo() {
    assertThatThrownBy(
            () ->
                new Alcance(
                    TipoAlcance.TODOS,
                    Set.of(INGENIERIA),
                    Set.of(),
                    Set.of(),
                    "Todos los programas"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("un alcance de todos no lleva facultades ni programas");

    assertThatThrownBy(
            () ->
                new Alcance(
                    TipoAlcance.FACULTADES,
                    Set.of(),
                    Set.of(),
                    Set.of(),
                    "Los programas de una facultad"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("un alcance por facultades debe indicar al menos una");

    assertThatThrownBy(
            () ->
                new Alcance(
                    TipoAlcance.FACULTADES,
                    Set.of(INGENIERIA),
                    Set.of(SISTEMAS.id()),
                    Set.of(),
                    "Los programas de una facultad"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("un alcance por facultades no lleva programas");

    assertThatThrownBy(
            () -> new Alcance(TipoAlcance.PROGRAMAS, Set.of(), Set.of(), Set.of(), "Un programa"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("un alcance por programas debe indicar al menos uno");

    assertThatThrownBy(
            () ->
                new Alcance(
                    TipoAlcance.PROGRAMAS,
                    Set.of(INGENIERIA),
                    Set.of(SISTEMAS.id()),
                    Set.of(),
                    "Un programa"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("un alcance por programas no lleva facultades");
  }

  @Test
  void tt03_alcancePorProgramasRechazaExclusionesAjenas() {
    ProgramaId ajeno = ProgramaId.nuevo();

    assertThatThrownBy(
            () ->
                new Alcance(
                    TipoAlcance.PROGRAMAS,
                    Set.of(),
                    Set.of(SISTEMAS.id()),
                    Set.of(ajeno),
                    "El programa de Ingeniería de Sistemas"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("las exclusiones deben ser programas del alcance");
  }

  @Test
  void tt03_alcanceCopiaLosProgramasYNoLosDejaModificar() {
    Set<ProgramaId> programas = new HashSet<>();
    programas.add(SISTEMAS.id());

    Alcance alcance =
        new Alcance(
            TipoAlcance.PROGRAMAS,
            Set.of(),
            programas,
            Set.of(),
            "El programa de Ingeniería de Sistemas");
    programas.clear();

    assertThat(alcance.programas()).containsExactly(SISTEMAS.id());
    assertThatThrownBy(() -> alcance.programas().add(CIVIL.id()))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void tt03_alcanceCopiaLasFacultadesYNoLasDejaModificar() {
    Set<Facultad> facultades = new HashSet<>();
    facultades.add(INGENIERIA);

    Alcance alcance =
        new Alcance(
            TipoAlcance.FACULTADES,
            facultades,
            Set.of(),
            Set.of(),
            "Los programas de la Facultad de Ingeniería");
    facultades.clear();

    assertThat(alcance.facultades()).containsExactly(INGENIERIA);
    assertThatThrownBy(() -> alcance.facultades().add(MEDICINA))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void tt03_alcanceRechazaTextoVacioYColeccionesNulas() {
    assertThatThrownBy(() -> new Alcance(TipoAlcance.TODOS, Set.of(), Set.of(), Set.of(), " "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("texto no puede ser vacío");
    assertThatThrownBy(
            () -> new Alcance(TipoAlcance.TODOS, null, Set.of(), Set.of(), "Todos los programas"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("facultades no puede ser null");
  }

  @Test
  void tt03_alcanceRechazaProgramaNuloAlComprobar() {
    Alcance alcance =
        new Alcance(
            TipoAlcance.TODOS, Set.of(), Set.of(), Set.of(), "Todos los programas de pregrado");

    assertThatThrownBy(() -> alcance.comprende(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("programa no puede ser null");
  }
}
