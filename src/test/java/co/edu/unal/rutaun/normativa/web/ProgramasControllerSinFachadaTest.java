package co.edu.unal.rutaun.normativa.web;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * UC06-HU01-T1 (#101): mientras Normativa no publique su implementación real de la fachada de
 * consulta, la aplicación arranca y las páginas dicen que la consulta no está disponible.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProgramasControllerSinFachadaTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void uc06_hu01_sinFachadaLaListaDeProgramasDiceQueNoEstaDisponible() throws Exception {
    mockMvc
        .perform(get("/programas"))
        .andExpect(status().isServiceUnavailable())
        .andExpect(content().string(containsString("no está disponible")));
  }

  @Test
  void uc06_hu01_sinFachadaLaPaginaDeReglasDiceQueNoEstaDisponible() throws Exception {
    mockMvc
        .perform(get("/programas/" + UUID.randomUUID() + "/reglas"))
        .andExpect(status().isServiceUnavailable())
        .andExpect(content().string(containsString("no está disponible")));
  }
}
