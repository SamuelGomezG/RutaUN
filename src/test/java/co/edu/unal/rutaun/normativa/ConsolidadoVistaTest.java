package co.edu.unal.rutaun.normativa;

import static co.edu.unal.rutaun.normativa.ClasificacionRegla.DEL_PLAN;
import static co.edu.unal.rutaun.normativa.ClasificacionRegla.INFORMATIVA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.rutaun.compartido.EstadoRegla;
import co.edu.unal.rutaun.compartido.ProgramaId;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ConsolidadoVistaTest {

  private static final ReglaVista REGLA =
      new ReglaVista(
          "Regla de prueba",
          Optional.of(
              new FuenteVista(
                  new NormaVista(TipoNorma.ACUERDO, "1", 2026, "Consejo de Facultad"),
                  "art. 1",
                  Optional.empty())),
          EstadoRegla.VIGENTE,
          false,
          DEL_PLAN);

  @Test
  void tt02_consolidadoVistaCopiaElMapaYSusListas() {
    List<ReglaVista> reglas = new ArrayList<>(List.of(REGLA));
    Map<String, List<ReglaVista>> porCategoria = new HashMap<>();
    porCategoria.put("Carga mínima o máxima por periodo", reglas);

    ConsolidadoVista vista =
        new ConsolidadoVista(ProgramaId.nuevo(), 1, LocalDate.of(2026, 10, 1), porCategoria);
    reglas.clear();
    porCategoria.clear();

    assertThat(vista.reglasPorCategoria()).containsOnlyKeys("Carga mínima o máxima por periodo");
    assertThat(vista.reglasPorCategoria().get("Carga mínima o máxima por periodo"))
        .containsExactly(REGLA);
  }

  @Test
  void tt02_consolidadoVistaNoSePuedeModificarPorFuera() {
    ConsolidadoVista vista =
        new ConsolidadoVista(
            ProgramaId.nuevo(),
            1,
            LocalDate.of(2026, 10, 1),
            Map.of("Carga mínima o máxima por periodo", List.of(REGLA)));

    assertThatThrownBy(() -> vista.reglasPorCategoria().put("Otra", List.of()))
        .isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(
            () -> vista.reglasPorCategoria().get("Carga mínima o máxima por periodo").add(REGLA))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void tt02_consolidadoVistaRechazaVersionNegativa() {
    assertThatThrownBy(
            () -> new ConsolidadoVista(ProgramaId.nuevo(), -1, LocalDate.of(2026, 10, 1), Map.of()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("version no puede ser negativo");
  }

  @Test
  void tt02_consolidadoVistaConservaElOrdenDeLasCategorias() {
    Map<String, List<ReglaVista>> porCategoria = new LinkedHashMap<>();
    porCategoria.put("Carga mínima o máxima por periodo", List.of(REGLA));
    porCategoria.put("Prerrequisitos y habilitación", List.of(REGLA));
    porCategoria.put("Otra", List.of(REGLA));

    ConsolidadoVista vista =
        new ConsolidadoVista(ProgramaId.nuevo(), 1, LocalDate.of(2026, 10, 1), porCategoria);

    assertThat(vista.reglasPorCategoria().keySet())
        .containsExactly(
            "Carga mínima o máxima por periodo", "Prerrequisitos y habilitación", "Otra");
  }

  @Test
  void tt02_consolidadoVistaRechazaCategoriasVaciasONulas() {
    Map<String, List<ReglaVista>> conCategoriaVacia = new LinkedHashMap<>();
    conCategoriaVacia.put(" ", List.of(REGLA));
    assertThatThrownBy(
            () ->
                new ConsolidadoVista(
                    ProgramaId.nuevo(), 1, LocalDate.of(2026, 10, 1), conCategoriaVacia))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("categoria no puede ser vacío");

    Map<String, List<ReglaVista>> conListaNula = new LinkedHashMap<>();
    conListaNula.put("Carga mínima o máxima por periodo", null);
    assertThatThrownBy(
            () ->
                new ConsolidadoVista(
                    ProgramaId.nuevo(), 1, LocalDate.of(2026, 10, 1), conListaNula))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("reglas no puede ser null");
  }

  @Test
  void tt02_uc06_ext4a_consolidadoVistaSinReglasConservaVersionYFecha() {
    ConsolidadoVista vista =
        new ConsolidadoVista(ProgramaId.nuevo(), 4, LocalDate.of(2026, 10, 1), Map.of());

    assertThat(vista.sinReglas()).isTrue();
    assertThat(vista.version()).isEqualTo(4);
    assertThat(vista.fecha()).isEqualTo(LocalDate.of(2026, 10, 1));
  }

  @Test
  void tt02_consolidadoVistaConReglasNoEstaSinReglas() {
    ConsolidadoVista vista =
        new ConsolidadoVista(
            ProgramaId.nuevo(),
            1,
            LocalDate.of(2026, 10, 1),
            Map.of("Carga mínima o máxima por periodo", List.of(REGLA)));

    assertThat(vista.sinReglas()).isFalse();
  }

  @Test
  void tt02_consolidadoVistaRechazaUnaCategoriaSinReglas() {
    assertThatThrownBy(
            () ->
                new ConsolidadoVista(
                    ProgramaId.nuevo(),
                    1,
                    LocalDate.of(2026, 10, 1),
                    Map.of("Carga mínima o máxima por periodo", List.of())))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("reglas no puede ser vacío");
  }

  @Test
  void tt02_reglaVistaAceptaFuenteAusentePeroNoOptionalNull() {
    assertThat(
            new ReglaVista("Regla", Optional.empty(), EstadoRegla.EN_REVISION, false, DEL_PLAN)
                .fuente())
        .isEmpty();
    assertThatThrownBy(() -> new ReglaVista("Regla", null, EstadoRegla.VIGENTE, false, DEL_PLAN))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("fuente no puede ser null");
    assertThatThrownBy(
            () -> new ReglaVista(" ", Optional.empty(), EstadoRegla.VIGENTE, false, DEL_PLAN))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("enunciado no puede ser vacío");
    assertThatThrownBy(() -> new ReglaVista("Regla", Optional.empty(), null, false, DEL_PLAN))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("estado no puede ser null");
  }

  @Test
  void tt02_uc06_ext6c_reglaVistaDiceSiElPlanLaAplicaOSoloLaInforma() {
    assertThat(
            new ReglaVista("Regla", Optional.empty(), EstadoRegla.VIGENTE, false, INFORMATIVA)
                .clasificacion())
        .isEqualTo(INFORMATIVA);
    assertThatThrownBy(
            () -> new ReglaVista("Regla", Optional.empty(), EstadoRegla.VIGENTE, false, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("clasificacion no puede ser null");
  }
}
