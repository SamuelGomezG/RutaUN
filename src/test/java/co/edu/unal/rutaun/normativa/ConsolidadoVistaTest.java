package co.edu.unal.rutaun.normativa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.rutaun.compartido.EstadoRegla;
import co.edu.unal.rutaun.compartido.ProgramaId;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
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
          false);

  @Test
  void tt02_consolidadoVistaCopiaElMapaYSusListas() {
    List<ReglaVista> reglas = new ArrayList<>(List.of(REGLA));
    Map<String, List<ReglaVista>> porCategoria = new HashMap<>();
    porCategoria.put("Carga por periodo", reglas);

    ConsolidadoVista vista =
        new ConsolidadoVista(ProgramaId.nuevo(), 1, LocalDate.of(2026, 10, 1), porCategoria);
    reglas.clear();
    porCategoria.clear();

    assertThat(vista.reglasPorCategoria()).containsOnlyKeys("Carga por periodo");
    assertThat(vista.reglasPorCategoria().get("Carga por periodo")).containsExactly(REGLA);
  }

  @Test
  void tt02_consolidadoVistaNoSePuedeModificarPorFuera() {
    ConsolidadoVista vista =
        new ConsolidadoVista(
            ProgramaId.nuevo(), 1, LocalDate.of(2026, 10, 1), Map.of("Carga", List.of(REGLA)));

    assertThatThrownBy(() -> vista.reglasPorCategoria().put("Otra", List.of()))
        .isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(() -> vista.reglasPorCategoria().get("Carga").add(REGLA))
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
  void tt02_reglaVistaAceptaFuenteAusentePeroNoOptionalNull() {
    assertThat(new ReglaVista("Regla", Optional.empty(), EstadoRegla.EN_REVISION, false).fuente())
        .isEmpty();
    assertThatThrownBy(() -> new ReglaVista("Regla", null, EstadoRegla.VIGENTE, false))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("fuente no puede ser null");
    assertThatThrownBy(() -> new ReglaVista(" ", Optional.empty(), EstadoRegla.VIGENTE, false))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("enunciado no puede ser vacío");
    assertThatThrownBy(() -> new ReglaVista("Regla", Optional.empty(), null, false))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("estado no puede ser null");
  }
}
