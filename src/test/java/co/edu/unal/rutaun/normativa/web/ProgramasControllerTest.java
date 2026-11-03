package co.edu.unal.rutaun.normativa.web;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import co.edu.unal.rutaun.normativa.ConsultaNormativaFacade;
import co.edu.unal.rutaun.soporte.ConsultaNormativaFalsa;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** UC06 paso 2 (UC06-HU01-T1, #101): el visitante, sin sesión, ve los programas y elige uno. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProgramasControllerTest {

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
  void uc06_paso2_laPaginaPublicaListaLosProgramasCargados() throws Exception {
    mockMvc
        .perform(get("/programas"))
        .andExpect(status().isOk())
        .andExpect(view().name("publico/programas"))
        .andExpect(content().string(containsString("Cualquier programa")))
        .andExpect(content().string(containsString("Ingeniería de Sistemas y Computación")));
  }

  @Test
  void uc06_paso2_cadaProgramaEnlazaASusReglas() throws Exception {
    mockMvc
        .perform(get("/programas"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("href=\"" + RUTA_DE_SISTEMAS + "\"")));
  }

  @Test
  void uc06_paso2_elegirUnProgramaLlevaASusReglas() throws Exception {
    mockMvc
        .perform(get(RUTA_DE_SISTEMAS))
        .andExpect(status().isOk())
        .andExpect(view().name("publico/reglas"))
        .andExpect(content().string(containsString("Ingeniería de Sistemas y Computación")));
  }

  @Test
  void uc06_paso2_unProgramaQueNoEstaCargadoNoSeEncuentra() throws Exception {
    mockMvc
        .perform(get("/programas/" + UUID.randomUUID() + "/reglas"))
        .andExpect(status().isNotFound());
  }
}
