package co.edu.unal.rutaun.incidentes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.rutaun.compartido.EventoDeDominio;
import co.edu.unal.rutaun.compartido.ProgramaId;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class IncidenteDetectadoTest {

  @Test
  void tt02_incidenteDeProgramaEsUnEventoDeDominio() {
    IncidenteDetectado incidente =
        new IncidenteDePrograma(
            "UC06", "ext. 4a", ProgramaId.nuevo(), 1, "consolidado sin reglas", List.of());

    assertThat(incidente).isInstanceOf(EventoDeDominio.class);
  }

  @Test
  void tt02_incidenteInternoEsUnEventoDeDominio() {
    IncidenteDetectado incidente =
        new IncidenteInterno("UC01", "paso 2", "falta el periodo en curso", List.of());

    assertThat(incidente).isInstanceOf(EventoDeDominio.class);
  }

  @Test
  void tt02_incidenteDeProgramaLlevaProgramaVersionYElementos() {
    ProgramaId programa = ProgramaId.nuevo();

    IncidenteDePrograma incidente =
        new IncidenteDePrograma("UC06", "ext. 6e", programa, 3, "regla sin fuente", List.of("R-1"));

    assertThat(incidente.programa()).isEqualTo(programa);
    assertThat(incidente.versionDelConsolidado()).isEqualTo(3);
    assertThat(incidente.elementos()).containsExactly("R-1");
  }

  @Test
  void tt02_incidenteInternoAceptaNoLlevarProgramaNiVersion() {
    IncidenteInterno incidente =
        new IncidenteInterno("UC01", "paso 2", "falta el periodo en curso", List.of());

    assertThat(incidente.casoDeUso()).isEqualTo("UC01");
    assertThat(incidente.paso()).isEqualTo("paso 2");
    assertThat(incidente.causa()).isEqualTo("falta el periodo en curso");
    assertThat(incidente.elementos()).isEmpty();
  }

  @Test
  void tt02_losIncidentesRechazanCasoDeUsoPasoOCausaVacios() {
    assertThatThrownBy(() -> new IncidenteInterno(" ", "paso", "causa", List.of()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("casoDeUso no puede ser vacío");
    assertThatThrownBy(() -> new IncidenteInterno("UC06", "", "causa", List.of()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("paso no puede ser vacío");
    assertThatThrownBy(() -> new IncidenteInterno("UC06", "paso", "", List.of()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("causa no puede ser vacío");
    assertThatThrownBy(() -> new IncidenteDePrograma("UC06", "paso", null, 1, "causa", List.of()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("programa no puede ser null");
  }

  @Test
  void tt02_elIncidenteDeProgramaRechazaUnaVersionCero() {
    assertThatThrownBy(
            () ->
                new IncidenteDePrograma(
                    "UC06", "ext. 4a", ProgramaId.nuevo(), 0, "consolidado sin reglas", List.of()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("versionDelConsolidado debe ser mayor que cero");
  }

  @Test
  void tt02_losIncidentesCopianLosElementos() {
    List<String> elementos = new ArrayList<>(List.of("R-1"));
    IncidenteDetectado incidente =
        new IncidenteDePrograma(
            "UC06", "ext. 6e", ProgramaId.nuevo(), 3, "regla sin fuente", elementos);

    elementos.add("R-2");

    assertThat(incidente.elementos()).containsExactly("R-1");
    assertThatThrownBy(() -> incidente.elementos().add("R-3"))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void tt02_losIncidentesExponenLosCamposComunes() {
    IncidenteDetectado conPrograma =
        new IncidenteDePrograma("UC06", "ext. 4a", ProgramaId.nuevo(), 1, "sin reglas", List.of());
    IncidenteDetectado interno =
        new IncidenteInterno("UC01", "paso 2", "falta el periodo en curso", List.of("periodo"));

    assertThat(conPrograma.casoDeUso()).isEqualTo("UC06");
    assertThat(conPrograma.paso()).isEqualTo("ext. 4a");
    assertThat(conPrograma.causa()).isEqualTo("sin reglas");
    assertThat(interno.casoDeUso()).isEqualTo("UC01");
    assertThat(interno.paso()).isEqualTo("paso 2");
    assertThat(interno.causa()).isEqualTo("falta el periodo en curso");
    assertThat(interno.elementos()).containsExactly("periodo");
  }
}
