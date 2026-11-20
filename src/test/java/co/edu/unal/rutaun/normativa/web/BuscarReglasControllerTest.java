package co.edu.unal.rutaun.normativa.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import co.edu.unal.rutaun.normativa.ConsultaNormativaFacade;
import co.edu.unal.rutaun.soporte.ConsultaNormativaFalsa;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * UC06 paso 6 (UC06-HU02, #35): el visitante busca reglas por texto y las filtra por categoría y
 * por estado, incluidas las que ya no rigen. Datos de {@link ConsultaNormativaFalsa}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BuscarReglasControllerTest {

  private static final String RUTA_DE_SISTEMAS =
      "/programas/" + ConsultaNormativaFalsa.SISTEMAS.valor() + "/reglas";

  private static final String CARGA = "el mínimo es de 10 créditos por periodo";
  private static final String PRERREQUISITO = "la materia B exige haber aprobado la materia A";
  private static final String TEMPORAL = "medida temporal para el periodo 2026-2";
  private static final String YA_NO_RIGE = "el máximo era de 20 créditos por periodo";

  /** Pone la fachada falsa en lugar de la real. */
  @TestConfiguration
  static class FachadaFalsa {

    @Bean
    @Primary
    ConsultaNormativaFacade consultaNormativa() {
      return ConsultaNormativaFalsa.conReglas();
    }
  }

  @Autowired private MockMvc mockMvc;

  private String pagina(String consulta) throws Exception {
    return mockMvc
        .perform(get(RUTA_DE_SISTEMAS + consulta))
        .andExpect(status().isOk())
        .andExpect(view().name("publico/reglas"))
        .andReturn()
        .getResponse()
        .getContentAsString();
  }

  @Test
  void uc06_paso6_laPaginaOfreceBuscarPorTextoYFiltrarPorCategoriaYPorEstado() throws Exception {
    String pagina = pagina("");

    assertThat(pagina)
        .contains("name=\"texto\"")
        .contains("name=\"categoria\"")
        .contains("name=\"estado\"")
        .contains("Prerrequisitos y habilitación")
        .contains("Ya no rige");
  }

  @Test
  void uc06_paso6_sinBusquedaMuestraTodasLasQueRigenYNingunaQueYaNoRija() throws Exception {
    String pagina = pagina("");

    assertThat(pagina).contains(CARGA, PRERREQUISITO, TEMPORAL).doesNotContain(YA_NO_RIGE);
  }

  @Test
  void uc06_paso6_buscarPorTextoMuestraSoloLasReglasQueLoContienen() throws Exception {
    assertThat(pagina("?texto=mínimo")).contains(CARGA).doesNotContain(PRERREQUISITO, TEMPORAL);
    assertThat(pagina("?texto=materia")).contains(PRERREQUISITO).doesNotContain(CARGA, TEMPORAL);
  }

  @Test
  void uc06_paso6_filtrarPorCategoriaMuestraSoloEsaCategoria() throws Exception {
    String pagina = pagina("?categoria=Otra");

    assertThat(pagina).contains(TEMPORAL).doesNotContain(CARGA, PRERREQUISITO);
  }

  @Test
  void uc06_paso6_elTextoYLosFiltrosSeCombinan() throws Exception {
    assertThat(pagina("?texto=periodo&categoria=Otra")).contains(TEMPORAL).doesNotContain(CARGA);
    assertThat(pagina("?texto=materia&categoria=Otra")).doesNotContain(PRERREQUISITO);
  }

  @Test
  void uc06_paso6_laPaginaConservaLoQueSeBusco() throws Exception {
    String pagina = pagina("?texto=mínimo&categoria=Otra");

    assertThat(pagina).contains("value=\"mínimo\"").contains("selected=\"selected\"");
  }

  @Test
  void uc06_ext5b_filtrarPorYaNoRigeMuestraEsasReglasConSuSenalYSuVigencia() throws Exception {
    String pagina = pagina("?estado=YA_NO_RIGE");

    assertThat(pagina)
        .contains(YA_NO_RIGE, "<em>ya no rige</em>", "desde 2020-01-01", "hasta 2025-12-31");
  }

  @Test
  void uc06_ext5b_lasQueYaNoRigenNoSeMezclanConLasVigentes() throws Exception {
    String pagina = pagina("?estado=YA_NO_RIGE");

    assertThat(pagina).doesNotContain(CARGA, PRERREQUISITO, TEMPORAL, "<em>vigente</em>");
    assertThat(pagina("?estado=VIGENTE")).doesNotContain(YA_NO_RIGE);
  }

  @Test
  void uc06_ext5a_sinResultadosSugiereRevisarElTextoYRecuerdaRegimenLegal() throws Exception {
    mockMvc
        .perform(get(RUTA_DE_SISTEMAS + "?texto=nada que ver"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("No se encontraron reglas")))
        .andExpect(content().string(containsString("Revisa el texto o quita algún filtro")))
        .andExpect(
            content()
                .string(
                    containsString(
                        "RutaUN no puede confirmar que la regla no exista: debe consultarse"
                            + " Régimen Legal")))
        .andExpect(content().string(not(containsString("problema que ya se informó"))));
  }

  @Test
  void uc06_ext5a_laBusquedaSinResultadosNoDiceQueElConsolidadoTengaUnProblema() throws Exception {
    String pagina = pagina("?categoria=Materias");

    assertThat(pagina)
        .contains("No se encontraron reglas")
        .doesNotContain("No hay reglas para mostrar");
  }

  @Test
  void uc06_paso6_unEstadoQueNoExisteSeRechaza() throws Exception {
    mockMvc.perform(get(RUTA_DE_SISTEMAS + "?estado=INVENTADO")).andExpect(status().isBadRequest());
  }
}
