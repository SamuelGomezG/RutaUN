package co.edu.unal.rutaun.normativa.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import co.edu.unal.rutaun.compartido.ReglaId;
import co.edu.unal.rutaun.normativa.ConsultaNormativaFacade;
import co.edu.unal.rutaun.soporte.ConsultaNormativaFalsa;
import java.util.UUID;
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
 * UC06 paso 8 y ext. 8a, 9a (UC06-HU03, #36): el visitante abre una regla completa, ve su fuente,
 * su vigencia, su alcance y la cadena de normas, y puede abrir el acuerdo en Régimen Legal. Datos
 * de {@link ConsultaNormativaFalsa}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReglaCompletaControllerTest {

  private static String ruta(ReglaId regla) {
    return "/programas/" + ConsultaNormativaFalsa.SISTEMAS.valor() + "/reglas/" + regla.valor();
  }

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

  @Test
  void uc06_paso8_muestraElEnunciadoLaFuenteLaVigenciaYElAlcance() throws Exception {
    mockMvc
        .perform(get(ruta(ConsultaNormativaFalsa.ID_PRERREQUISITO)))
        .andExpect(status().isOk())
        .andExpect(view().name("publico/regla"))
        .andExpect(content().string(containsString("la materia B exige haber aprobado")))
        .andExpect(content().string(containsString("Acuerdo")))
        .andExpect(content().string(containsString("1 de 2026")))
        .andExpect(content().string(containsString("art. 2")))
        .andExpect(content().string(containsString("Fecha de aplicación: 2026-01-15")))
        .andExpect(content().string(containsString("sin fecha de fin")))
        .andExpect(content().string(containsString("uno o varios programas concretos")))
        .andExpect(content().string(containsString("El plan")))
        .andExpect(content().string(containsString("aplica esta regla")));
  }

  @Test
  void uc06_paso8_unaReglaTemporalDiceHastaCuandoRige() throws Exception {
    mockMvc
        .perform(get(ruta(ConsultaNormativaFalsa.ID_TEMPORAL)))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("temporal")))
        .andExpect(content().string(containsString("2026-12-31")))
        .andExpect(content().string(containsString("solo informa esta regla")));
  }

  @Test
  void uc06_ext8a_conVariasNormasMuestraLaCadenaYMarcaLaQueRige() throws Exception {
    String pagina =
        mockMvc
            .perform(get(ruta(ConsultaNormativaFalsa.ID_TEMPORAL)))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Normas que han afectado esta regla")))
            .andExpect(
                content()
                    .string(
                        stringContainsInOrder(
                            "Expide la regla:",
                            "2 de 2026",
                            "La modifica:",
                            "3 de 2026",
                            "rige hoy")))
            .andReturn()
            .getResponse()
            .getContentAsString();

    assertThat(pagina.split("rige hoy", -1)).hasSize(2);
  }

  @Test
  void uc06_ext8a_unaReglaSinOtrasNormasDiceQueNingunaLaHaAfectado() throws Exception {
    mockMvc
        .perform(get(ruta(ConsultaNormativaFalsa.ID_PRERREQUISITO)))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Ninguna otra norma ha afectado")));
  }

  @Test
  void uc06_paso8_conEnlaceOfreceAbrirElAcuerdoEnRegimenLegal() throws Exception {
    mockMvc
        .perform(get(ruta(ConsultaNormativaFalsa.ID_PRERREQUISITO)))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("href=\"" + ConsultaNormativaFalsa.ENLACE)))
        .andExpect(content().string(containsString("Abrir el acuerdo en Régimen Legal")));
  }

  @Test
  void uc06_ext9a_sinEnlaceMuestraLaIdentificacionCompletaYLoDice() throws Exception {
    mockMvc
        .perform(get(ruta(ConsultaNormativaFalsa.ID_CARGA)))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("El enlace a Régimen Legal no está disponible")))
        .andExpect(content().string(containsString("1 de 2026")))
        .andExpect(content().string(containsString("art. 1")))
        .andExpect(content().string(containsString("Consejo de Facultad de Ingeniería")));
  }

  @Test
  void uc06_paso8_aclaraQueLaFuenteOficialEsRegimenLegal() throws Exception {
    mockMvc
        .perform(get(ruta(ConsultaNormativaFalsa.ID_CARGA)))
        .andExpect(status().isOk())
        .andExpect(
            content()
                .string(
                    containsString(
                        "RutaUN no es la fuente oficial: la fuente oficial es el documento"
                            + " publicado en Régimen Legal.")));
  }

  @Test
  void uc06_paso8_laPaginaDeReglasEnlazaCadaReglaConSuDetalle() throws Exception {
    mockMvc
        .perform(get("/programas/" + ConsultaNormativaFalsa.SISTEMAS.valor() + "/reglas"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString(ruta(ConsultaNormativaFalsa.ID_CARGA))))
        .andExpect(content().string(containsString(ruta(ConsultaNormativaFalsa.ID_PRERREQUISITO))))
        .andExpect(content().string(containsString(ruta(ConsultaNormativaFalsa.ID_TEMPORAL))));
  }

  @Test
  void uc06_paso8_unaReglaQueLaConsultaNoMuestraResponde404() throws Exception {
    mockMvc.perform(get(ruta(new ReglaId(UUID.randomUUID())))).andExpect(status().isNotFound());
  }

  @Test
  void uc06_paso8_unProgramaNoCargadoResponde404() throws Exception {
    mockMvc
        .perform(
            get(
                "/programas/"
                    + UUID.randomUUID()
                    + "/reglas/"
                    + ConsultaNormativaFalsa.ID_CARGA.valor()))
        .andExpect(status().isNotFound());
  }
}
