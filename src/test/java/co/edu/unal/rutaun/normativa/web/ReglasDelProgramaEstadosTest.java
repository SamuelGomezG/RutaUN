package co.edu.unal.rutaun.normativa.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.unal.rutaun.compartido.EstadoRegla;
import co.edu.unal.rutaun.compartido.ProgramaId;
import co.edu.unal.rutaun.normativa.ClasificacionRegla;
import co.edu.unal.rutaun.normativa.ConsolidadoVista;
import co.edu.unal.rutaun.normativa.ConsultaNormativaFacade;
import co.edu.unal.rutaun.normativa.FuenteVista;
import co.edu.unal.rutaun.normativa.NormaVista;
import co.edu.unal.rutaun.normativa.ProgramaVista;
import co.edu.unal.rutaun.normativa.ReglaVista;
import co.edu.unal.rutaun.normativa.TipoNorma;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * UC06 paso 4 (UC06-HU01-T2, #102): cada regla muestra su estado y, si lo es, la etiqueta
 * «temporal». La fachada falsa del módulo solo trae reglas vigentes, así que esta prueba usa la
 * suya, con una regla por estado, un programa sin reglas y un programa sin versión vigente.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReglasDelProgramaEstadosTest {

  private static final ProgramaId CON_ESTADOS = programaId(1);
  private static final ProgramaId SIN_REGLAS = programaId(2);
  private static final ProgramaId SIN_VERSION = programaId(3);

  private static ProgramaId programaId(int numero) {
    return new ProgramaId(UUID.fromString("00000000-0000-0000-0000-00000000010" + numero));
  }

  private static ReglaVista regla(String enunciado, EstadoRegla estado, boolean temporal) {
    return new ReglaVista(
        enunciado, Optional.empty(), estado, temporal, ClasificacionRegla.DEL_PLAN);
  }

  private static ReglaVista regla(
      String enunciado, EstadoRegla estado, boolean temporal, FuenteVista fuente) {
    return new ReglaVista(
        enunciado, Optional.of(fuente), estado, temporal, ClasificacionRegla.DEL_PLAN);
  }

  private static FuenteVista fuente(TipoNorma tipo, String numero, String articulo) {
    return new FuenteVista(
        new NormaVista(tipo, numero, 2026, "Consejo de prueba"), articulo, Optional.empty());
  }

  private static String ruta(ProgramaId id) {
    return "/programas/" + id.valor() + "/reglas";
  }

  /** Fachada con tres programas de prueba, uno por situación. */
  @TestConfiguration
  static class FachadaConEstados {

    @Bean
    ConsultaNormativaFacade consultaNormativa() {
      return new ConsultaNormativaFacade() {
        @Override
        public List<ProgramaVista> programas() {
          return List.of();
        }

        @Override
        public Optional<ProgramaVista> programa(ProgramaId id) {
          return Optional.of(new ProgramaVista(id, "Programa de prueba", "0000", "Facultad"));
        }

        @Override
        public Optional<ConsolidadoVista> consolidadoVigente(ProgramaId id) {
          if (CON_ESTADOS.equals(id)) {
            return Optional.of(
                new ConsolidadoVista(
                    id,
                    3,
                    LocalDate.of(2026, 10, 15),
                    Map.of(
                        "Otra",
                        List.of(
                            regla(
                                "Regla uno",
                                EstadoRegla.VIGENTE,
                                false,
                                fuente(TipoNorma.RESOLUCION, "5", "art. 3")),
                            regla("Regla dos", EstadoRegla.PROGRAMADA, false),
                            regla(
                                "Regla tres",
                                EstadoRegla.YA_NO_RIGE,
                                false,
                                fuente(TipoNorma.CIRCULAR, "11", "num. 2")),
                            regla("Regla cuatro", EstadoRegla.EN_REVISION, true)))));
          }
          if (SIN_REGLAS.equals(id)) {
            return Optional.of(new ConsolidadoVista(id, 2, LocalDate.of(2026, 10, 1), Map.of()));
          }
          return Optional.empty();
        }
      };
    }
  }

  @Autowired private MockMvc mockMvc;

  @Test
  void uc06_paso4_cadaReglaMuestraSuEstado() throws Exception {
    mockMvc
        .perform(get(ruta(CON_ESTADOS)))
        .andExpect(status().isOk())
        .andExpect(
            content()
                .string(
                    stringContainsInOrder(
                        List.of(
                            "Regla uno",
                            "<em>vigente</em>",
                            "Regla dos",
                            "<em>programada</em>",
                            "Regla tres",
                            "<em>ya no rige</em>",
                            "Regla cuatro",
                            "<em>en revisión</em>"))));
  }

  @Test
  void uc06_paso4_laEtiquetaTemporalAparecePegadaALaReglaTemporal() throws Exception {
    List<String> enOrden = List.of("Regla cuatro", "<strong>temporal</strong>");

    mockMvc
        .perform(get(ruta(CON_ESTADOS)))
        .andExpect(status().isOk())
        .andExpect(content().string(stringContainsInOrder(enOrden)));
  }

  @Test
  void uc06_gm2_laReglaConFuenteLaCitaYLaQueNoTieneDiceSinFuenteRegistrada() throws Exception {
    String pagina =
        mockMvc
            .perform(get(ruta(CON_ESTADOS)))
            .andExpect(status().isOk())
            .andExpect(
                content()
                    .string(
                        stringContainsInOrder(
                            List.of(
                                "Regla uno",
                                "Resolución",
                                "5 de 2026, Consejo de prueba, art. 3",
                                "Regla dos",
                                "sin fuente registrada",
                                "Regla tres",
                                "Circular",
                                "11 de 2026, Consejo de prueba, num. 2",
                                "Regla cuatro",
                                "sin fuente registrada"))))
            .andReturn()
            .getResponse()
            .getContentAsString();

    String laPrimera = pagina.substring(pagina.indexOf("Regla uno"), pagina.indexOf("Regla dos"));
    assertThat(laPrimera).doesNotContain("sin fuente registrada");
  }

  @Test
  void uc06_paso4_conVersionSinReglasLaPaginaLoDice() throws Exception {
    mockMvc
        .perform(get(ruta(SIN_REGLAS)))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Versión del consolidado: 2")))
        .andExpect(content().string(containsString("No hay reglas para mostrar.")));
  }

  @Test
  void uc06_paso4_unProgramaSinVersionVigenteNoSeEncuentra() throws Exception {
    mockMvc.perform(get(ruta(SIN_VERSION))).andExpect(status().isNotFound());
  }
}
