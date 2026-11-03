package co.edu.unal.rutaun.normativa.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import co.edu.unal.rutaun.normativa.ConsultaNormativaFacade;
import co.edu.unal.rutaun.soporte.ConsultaNormativaFalsa;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * UC06 paso 4 (UC06-HU01-T2, #102): el visitante ve las reglas del programa agrupadas por
 * categoría, con la versión del consolidado y su fecha. Datos de {@link ConsultaNormativaFalsa}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReglasDelProgramaControllerTest {

  private static final String RUTA_DE_SISTEMAS =
      "/programas/" + ConsultaNormativaFalsa.SISTEMAS.valor() + "/reglas";

  /** Pone la fachada falsa en lugar de la implementación real, que aún no existe. */
  @TestConfiguration
  static class FachadaFalsa {

    @Bean
    ConsultaNormativaFacade consultaNormativa() {
      return ConsultaNormativaFalsa.conReglas();
    }
  }

  @Autowired private MockMvc mockMvc;

  @Test
  void uc06_paso4_muestraLasReglasAgrupadasPorCategoria() throws Exception {
    mockMvc
        .perform(get(RUTA_DE_SISTEMAS))
        .andExpect(status().isOk())
        .andExpect(view().name("publico/reglas"))
        .andExpect(
            content()
                .string(
                    stringContainsInOrder(
                        List.of(
                            "Carga mínima o máxima por periodo",
                            "el mínimo es de 10 créditos por periodo",
                            "Prerrequisitos y habilitación",
                            "la materia B exige haber aprobado la materia A",
                            "Otra",
                            "medida temporal para el periodo 2026-2"))));
  }

  @Test
  void uc06_paso4_muestraLaVersionDelConsolidadoYSuFechaDeActualizacion() throws Exception {
    mockMvc
        .perform(get(RUTA_DE_SISTEMAS))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Versión del consolidado: 1")))
        .andExpect(content().string(containsString("Fecha de actualización: 2026-10-01")));
  }

  @Test
  void uc06_paso4_soloLaReglaTemporalLlevaLaEtiquetaTemporal() throws Exception {
    String pagina =
        mockMvc
            .perform(get(RUTA_DE_SISTEMAS))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    assertThat(pagina).containsOnlyOnce("<strong>temporal</strong>");
  }

  @Test
  void uc06_gm2_cadaReglaCitaSuFuenteConNumeroAnioOrganoYArticulo() throws Exception {
    String organo = "del Consejo de Facultad de Ingeniería (de prueba)";
    List<String> enOrden =
        List.of(
            "el mínimo es de 10 créditos por periodo",
            "Acuerdo",
            "1 de 2026 " + organo + ", art. 1",
            "la materia B exige haber aprobado la materia A",
            "Acuerdo",
            "1 de 2026 " + organo + ", art. 2",
            "medida temporal para el periodo 2026-2",
            "Acuerdo",
            "2 de 2026 " + organo + ", art. 1");

    mockMvc
        .perform(get(RUTA_DE_SISTEMAS))
        .andExpect(status().isOk())
        .andExpect(content().string(stringContainsInOrder(enOrden)))
        .andExpect(content().string(not(containsString("sin fuente registrada"))));
  }

  @Test
  void uc06_ge5_recuerdaQueLaFuenteOficialEsElDocumentoDeRegimenLegal() throws Exception {
    mockMvc
        .perform(get(RUTA_DE_SISTEMAS))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("RutaUN no es la fuente oficial")))
        .andExpect(content().string(containsString("Régimen Legal")));
  }
}
