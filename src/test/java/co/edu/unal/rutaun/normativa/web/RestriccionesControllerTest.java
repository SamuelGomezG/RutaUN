package co.edu.unal.rutaun.normativa.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import co.edu.unal.rutaun.compartido.CodigoMateria;
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
 * UC05 pasos 1 a 4 y ext. 3a, 4a, 4d (UC05-HU01, #27): el visitante, sin sesión, ve lo que exige
 * una materia, con la fuente, la fecha de aplicación y la vigencia de cada restricción. Datos de
 * {@link ConsultaNormativaFalsa}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RestriccionesControllerTest {

  private static String ruta(CodigoMateria materia) {
    return ruta(ConsultaNormativaFalsa.SISTEMAS.valor(), materia.valor());
  }

  private static String ruta(UUID programa, String materia) {
    return "/programas/" + programa + "/materias/" + materia + "/restricciones";
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
  void uc05_paso4_sinSesionMuestraLaMateriaYSusRestricciones() throws Exception {
    mockMvc
        .perform(get(ruta(ConsultaNormativaFalsa.MATERIA_CON_RESTRICCIONES)))
        .andExpect(status().isOk())
        .andExpect(view().name("publico/restricciones"))
        .andExpect(content().string(containsString("Materia B")))
        .andExpect(content().string(containsString("Ingeniería de Sistemas y Computación")))
        .andExpect(content().string(containsString("Prerrequisito")))
        .andExpect(content().string(containsString("Correquisito")))
        .andExpect(content().string(containsString("Materias que deben estar aprobadas")))
        .andExpect(content().string(containsString("Materias que deben cursarse")))
        .andExpect(content().string(containsString("Qué la satisface")));
  }

  @Test
  void uc05_ge2_cadaRestriccionConFuenteMuestraSuFuenteSuFechaYSuVigencia() throws Exception {
    mockMvc
        .perform(get(ruta(ConsultaNormativaFalsa.MATERIA_CON_RESTRICCIONES)))
        .andExpect(status().isOk())
        .andExpect(
            content()
                .string(
                    stringContainsInOrder(
                        "Lo establece el",
                        "Acuerdo",
                        "1 de 2026",
                        "art. 2",
                        "Fecha de aplicación")))
        .andExpect(content().string(containsString("2026-01-15")))
        .andExpect(content().string(containsString("sin fecha de fin")));
  }

  @Test
  void uc05_ext4a_unPrerrequisitoConAlternativasLasMuestraTodas() throws Exception {
    mockMvc
        .perform(get(ruta(ConsultaNormativaFalsa.MATERIA_CON_RESTRICCIONES)))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Materia C o Materia D")));
  }

  @Test
  void uc05_ext4d_unaRestriccionSinFuenteDiceSinFuenteRegistrada() throws Exception {
    String pagina =
        mockMvc
            .perform(get(ruta(ConsultaNormativaFalsa.MATERIA_CON_RESTRICCIONES)))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("sin fuente registrada")))
            .andReturn()
            .getResponse()
            .getContentAsString();

    assertThat(pagina.split("sin fuente registrada", -1)).hasSize(2);
    assertThat(pagina.split("Lo establece el", -1)).hasSize(3);
  }

  @Test
  void uc05_ext3a_unaMateriaSinRestriccionesLoDiceYRecuerdaQueNoEsOfertaNiCupo() throws Exception {
    mockMvc
        .perform(get(ruta(ConsultaNormativaFalsa.MATERIA_SIN_RESTRICCIONES)))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("no tiene restricciones")))
        .andExpect(
            content().string(containsString("no significa que esté ofrecida ni que haya cupo")))
        .andExpect(content().string(not(containsString("Qué la satisface"))));
  }

  @Test
  void uc05_ext4c_unaReglaDeHabilitacionSeMuestraComoRestriccionDeLaMateria() throws Exception {
    String pagina =
        mockMvc
            .perform(get(ruta(ConsultaNormativaFalsa.MATERIA_CON_HABILITACION)))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Regla de habilitación")))
            .andExpect(content().string(containsString("haber aprobado 40 créditos")))
            .andExpect(content().string(containsString("Lo establece el")))
            .andExpect(content().string(not(containsString("no tiene restricciones"))))
            .andReturn()
            .getResponse()
            .getContentAsString();

    assertThat(pagina.split("haber aprobado 40 créditos", -1)).hasSize(2);
  }

  @Test
  void uc05_ext4k_unaMateriaNoCursableLoDiceYRecuerdaQueElPlanNoLaUbica() throws Exception {
    mockMvc
        .perform(get(ruta(ConsultaNormativaFalsa.MATERIA_NO_CURSABLE)))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("No se puede cursar desde este programa")))
        .andExpect(content().string(containsString("no pueden inscribir")))
        .andExpect(content().string(containsString("El plan de carrera no la ubica")))
        .andExpect(content().string(not(containsString("no tiene restricciones"))))
        .andExpect(content().string(not(containsString("Qué la satisface"))));
  }

  @Test
  void uc05_ext4k_unaMateriaQueSeCursaNoLlevaLaSenal() throws Exception {
    mockMvc
        .perform(get(ruta(ConsultaNormativaFalsa.MATERIA_CON_RESTRICCIONES)))
        .andExpect(content().string(not(containsString("No se puede cursar desde este programa"))));
  }

  @Test
  void uc05_ge2_unaRestriccionProgramadaOEnRevisionLoSenalaYUnaVigenteNo() throws Exception {
    String conEstados = pagina(ConsultaNormativaFalsa.MATERIA_CON_ESTADOS);
    String vigentes = pagina(ConsultaNormativaFalsa.MATERIA_CON_RESTRICCIONES);

    assertThat(conEstados).contains("Programada:").contains("En revisión:");
    assertThat(vigentes).doesNotContain("Programada:").doesNotContain("En revisión:");
  }

  @Test
  void uc05_ext4e_soloLaRestriccionTemporalLlevaLaSenalTemporal() throws Exception {
    String conEstados = pagina(ConsultaNormativaFalsa.MATERIA_CON_ESTADOS);
    String vigentes = pagina(ConsultaNormativaFalsa.MATERIA_CON_RESTRICCIONES);

    assertThat(conEstados.split("<strong>temporal</strong>", -1)).hasSize(2);
    assertThat(vigentes).doesNotContain("<strong>temporal</strong>");
  }

  @Test
  void uc05_ge2_laVigenciaDiceHastaCuandoRigeOQueNoTieneFin() throws Exception {
    String conEstados = pagina(ConsultaNormativaFalsa.MATERIA_CON_ESTADOS);
    String vigentes = pagina(ConsultaNormativaFalsa.MATERIA_CON_RESTRICCIONES);

    assertThat(conEstados).contains("hasta 2026-12-31").contains("2027-01-15");
    assertThat(vigentes).contains("sin fecha de fin").doesNotContain("hasta 20");
  }

  private String pagina(CodigoMateria materia) throws Exception {
    return mockMvc
        .perform(get(ruta(materia)))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();
  }

  @Test
  void uc05_gm3_laExplicacionNuncaAfirmaQueAlguienPuedaONoInscribirLaMateria() throws Exception {
    for (CodigoMateria materia :
        new CodigoMateria[] {
          ConsultaNormativaFalsa.MATERIA_SIN_RESTRICCIONES,
          ConsultaNormativaFalsa.MATERIA_CON_RESTRICCIONES
        }) {
      mockMvc
          .perform(get(ruta(materia)))
          .andExpect(content().string(not(containsString("puede inscribir"))))
          .andExpect(content().string(not(containsString("no puede inscribir"))))
          .andExpect(content().string(not(containsString("podrá inscribir"))));
    }
  }

  @Test
  void uc05_ge5_incluyeElAvisoDeLoQueRutaUnNoVerifica() throws Exception {
    for (CodigoMateria materia :
        new CodigoMateria[] {
          ConsultaNormativaFalsa.MATERIA_SIN_RESTRICCIONES,
          ConsultaNormativaFalsa.MATERIA_CON_RESTRICCIONES
        }) {
      String pagina =
          mockMvc
              .perform(get(ruta(materia)))
              .andExpect(status().isOk())
              .andExpect(content().string(containsString("Aviso de lo que RutaUN no verifica")))
              .andExpect(content().string(containsString("la oferta de materias")))
              .andExpect(content().string(containsString("los cupos y sobrecupos")))
              .andReturn()
              .getResponse()
              .getContentAsString();

      assertThat(pagina.split("id=\"aviso-no-verifica\"", -1)).hasSize(2);
    }
  }

  @Test
  void uc05_paso4_cadaRestriccionOfreceAbrirLaReglaCompleta() throws Exception {
    mockMvc
        .perform(get(ruta(ConsultaNormativaFalsa.MATERIA_CON_RESTRICCIONES)))
        .andExpect(status().isOk())
        .andExpect(
            content()
                .string(
                    containsString(
                        "/programas/"
                            + ConsultaNormativaFalsa.SISTEMAS.valor()
                            + "/reglas/"
                            + ConsultaNormativaFalsa.ID_PRERREQUISITO.valor())))
        .andExpect(content().string(containsString("Ver la regla completa")));
  }

  @Test
  void uc05_paso2_unaMateriaQueNoFiguraResponde404() throws Exception {
    mockMvc.perform(get(ruta(new CodigoMateria("2999999")))).andExpect(status().isNotFound());
  }

  @Test
  void uc05_precondicion1_unProgramaNoCargadoResponde404() throws Exception {
    mockMvc
        .perform(
            get(ruta(UUID.randomUUID(), ConsultaNormativaFalsa.MATERIA_CON_RESTRICCIONES.valor())))
        .andExpect(status().isNotFound());
  }
}
