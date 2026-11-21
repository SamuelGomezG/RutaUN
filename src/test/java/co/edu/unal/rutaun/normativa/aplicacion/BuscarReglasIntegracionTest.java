package co.edu.unal.rutaun.normativa.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.unal.rutaun.compartido.EstadoRegla;
import co.edu.unal.rutaun.compartido.ProgramaId;
import co.edu.unal.rutaun.compartido.ReglaId;
import co.edu.unal.rutaun.incidentes.persistencia.IncidenteRepository;
import co.edu.unal.rutaun.normativa.BusquedaDeReglas;
import co.edu.unal.rutaun.normativa.ConsolidadoVista;
import co.edu.unal.rutaun.normativa.ConsultaNormativaFacade;
import co.edu.unal.rutaun.normativa.ReglaVista;
import co.edu.unal.rutaun.normativa.persistencia.ProgramaRepository;
import co.edu.unal.rutaun.soporte.PruebaDeIntegracion;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * UC06 paso 6 con la semilla real (UC06-HU02, #35): busca reglas por texto, las filtra por
 * categoría y por estado con la fachada real y por la página, y comprueba lo que pasa con las que
 * ya no rigen (ext. 5b) y cuando no hay resultados (ext. 5a). La semilla no trae reglas que ya no
 * rigen, así que la prueba pasa una a ese estado.
 *
 * <p>Lleva {@code @Transactional}: lo que carga se deshace al terminar.
 */
@PruebaDeIntegracion
@AutoConfigureMockMvc
@Transactional
class BuscarReglasIntegracionTest {

  private static final Logger LOG = LoggerFactory.getLogger(BuscarReglasIntegracionTest.class);

  private static final List<String> ACUERDOS =
      List.of(
          "semilla/acuerdo-003a-2022.json",
          "semilla/acuerdo-008-2008.json",
          "semilla/acuerdo-11-2023.json");

  private static final String CIENTO_SESENTA_Y_CINCO = "ciento sesenta y cinco (165)";

  @Autowired private ImportacionDeProgramasService importacionDeProgramas;
  @Autowired private ImportacionDeNormasService importacionDeNormas;
  @Autowired private ProgramaRepository programas;
  @Autowired private IncidenteRepository incidentes;
  @Autowired private ConsultaNormativaFacade consulta;
  @Autowired private MockMvc mockMvc;
  @Autowired private JdbcTemplate jdbc;
  @PersistenceContext private EntityManager entityManager;

  @BeforeEach
  void cargarLaSemilla() throws IOException {
    importacionDeProgramas.importar(
        new ClassPathResource("semilla/programas.json").getContentAsString(StandardCharsets.UTF_8));
    for (String archivo : ACUERDOS) {
      try (InputStream entrada = new ClassPathResource(archivo).getInputStream()) {
        importacionDeNormas.importar(entrada);
      }
    }
  }

  private ProgramaId sistemas() {
    return new ProgramaId(programas.findByCodigoSia("2879").orElseThrow().getId());
  }

  private List<ReglaVista> todas() {
    return reglasDe(consulta.consolidadoVigente(sistemas()).orElseThrow());
  }

  private List<ReglaVista> buscar(BusquedaDeReglas busqueda) {
    return reglasDe(consulta.buscarReglas(sistemas(), busqueda).orElseThrow());
  }

  private static List<ReglaVista> reglasDe(ConsolidadoVista consolidado) {
    return consolidado.reglasPorCategoria().values().stream().flatMap(List::stream).toList();
  }

  private ReglaId idDeLaReglaDeCreditos() {
    return todas().stream()
        .filter(regla -> regla.enunciado().contains(CIENTO_SESENTA_Y_CINCO))
        .map(ReglaVista::id)
        .findFirst()
        .orElseThrow();
  }

  /**
   * Pasa la regla a «ya no rige» en la versión 3 y le pone un «vigente hasta» ya cumplido; la deja
   * informativa, porque una regla temporal no puede ser del plan.
   */
  private void laReglaYaNoRige(ReglaId regla, LocalDate hasta) {
    entityManager.flush();
    jdbc.update(
        "UPDATE regla_en_version SET estado = 'YA_NO_RIGE' WHERE regla_id = ? AND version_id ="
            + " (SELECT id FROM version_consolidado WHERE programa_id = ? AND numero = 3)",
        regla.valor(),
        sistemas().valor());
    jdbc.update(
        "UPDATE regla SET vigente_desde = ?, vigente_hasta = ?, clasificacion = ? WHERE id = ?",
        LocalDate.of(2020, 1, 1),
        hasta,
        "INFORMATIVA",
        regla.valor());
    entityManager.flush();
    entityManager.clear();
  }

  /**
   * Pasa la regla a «ya no rige» en la versión 3 sin darle vigencia propia, y le pone a su norma un
   * «vigente hasta» ya cumplido; la deja informativa, porque una regla temporal no puede ser del
   * plan.
   */
  private void laReglaYaNoRigePorqueSuNormaVencio(ReglaId regla, LocalDate hasta) {
    entityManager.flush();
    jdbc.update(
        "UPDATE regla_en_version SET estado = 'YA_NO_RIGE' WHERE regla_id = ? AND version_id ="
            + " (SELECT id FROM version_consolidado WHERE programa_id = ? AND numero = 3)",
        regla.valor(),
        sistemas().valor());
    jdbc.update(
        "UPDATE regla SET vigente_desde = NULL, vigente_hasta = NULL, clasificacion = ?"
            + " WHERE id = ?",
        "INFORMATIVA",
        regla.valor());
    jdbc.update(
        "UPDATE norma SET vigente_hasta = ? WHERE id = (SELECT norma_id FROM regla WHERE id = ?)",
        hasta,
        regla.valor());
    entityManager.flush();
    entityManager.clear();
  }

  /** Le pone a la norma de la regla un «vigente hasta» distinto del que la regla pueda tener. */
  private void laNormaDeLaReglaVenceEl(ReglaId regla, LocalDate hasta) {
    entityManager.flush();
    jdbc.update(
        "UPDATE norma SET vigente_hasta = ? WHERE id = (SELECT norma_id FROM regla WHERE id = ?)",
        hasta,
        regla.valor());
    entityManager.flush();
    entityManager.clear();
  }

  @Test
  void uc06_paso6_sinBusquedaDevuelveElMismoConsolidadoQueElListadoNormal() {
    assertThat(consulta.buscarReglas(sistemas(), BusquedaDeReglas.TODAS))
        .isEqualTo(consulta.consolidadoVigente(sistemas()));
  }

  @Test
  void uc06_paso6_buscarPorTextoEncuentraLaReglaSinImportarTildesNiMayusculas() {
    List<ReglaVista> encontradas =
        buscar(BusquedaDeReglas.de("CIENTO SESENTA Y CINCO", null, null));

    assertThat(encontradas)
        .extracting(ReglaVista::id)
        .contains(idDeLaReglaDeCreditos())
        .hasSizeLessThan(todas().size());
  }

  @Test
  void uc06_paso6_buscarPorAcuerdoEncuentraLasReglasDeEseAcuerdo() {
    List<ReglaVista> delAcuerdo =
        todas().stream()
            .filter(regla -> regla.fuente().orElseThrow().norma().numero().equals("008"))
            .toList();

    List<ReglaVista> encontradas = buscar(BusquedaDeReglas.de("acuerdo 008", null, null));

    assertThat(delAcuerdo).isNotEmpty();
    assertThat(encontradas)
        .extracting(ReglaVista::id)
        .containsExactlyInAnyOrderElementsOf(delAcuerdo.stream().map(ReglaVista::id).toList())
        .hasSizeLessThan(todas().size());
  }

  @Test
  void uc06_paso6_filtrarPorCategoriaDevuelveSoloEsaCategoria() {
    String categoria = ConsultaNormativaService.PRERREQUISITOS_Y_HABILITACION;

    ConsolidadoVista consolidado =
        consulta.buscarReglas(sistemas(), BusquedaDeReglas.de(null, categoria, null)).orElseThrow();

    assertThat(consolidado.reglasPorCategoria()).containsOnlyKeys(categoria);
  }

  @Test
  void uc06_paso6_laCategoriaYElTextoSeCombinan() {
    ConsolidadoVista consolidado =
        consulta
            .buscarReglas(
                sistemas(),
                BusquedaDeReglas.de(
                    "CIENTO SESENTA Y CINCO", ConsultaNormativaService.MATERIAS, null))
            .orElseThrow();

    assertThat(consolidado.sinReglas()).isTrue();
  }

  @Test
  void uc06_paso6_lasCategoriasSonLasDelGlosarioEnSuOrden() {
    assertThat(consulta.categoriasDeReglas())
        .startsWith(
            ConsultaNormativaService.COMPONENTES_Y_CREDITOS, ConsultaNormativaService.MATERIAS)
        .endsWith(ConsultaNormativaService.REQUISITOS_DE_GRADO, ConsultaNormativaService.OTRA);
  }

  @Test
  void uc06_ext5b_unaReglaQueYaNoRigeSoloSaleAlPedirEseEstadoYConSuVigencia() {
    ReglaId regla = idDeLaReglaDeCreditos();
    laReglaYaNoRige(regla, LocalDate.of(2025, 12, 31));

    List<ReglaVista> normales = todas();
    List<ReglaVista> queYaNoRigen = buscar(BusquedaDeReglas.de(null, null, EstadoRegla.YA_NO_RIGE));

    assertThat(normales).extracting(ReglaVista::id).doesNotContain(regla);
    assertThat(queYaNoRigen).extracting(ReglaVista::id).containsExactly(regla);
    ReglaVista vencida = queYaNoRigen.getFirst();
    assertThat(vencida.estado()).isEqualTo(EstadoRegla.YA_NO_RIGE);
    assertThat(vencida.vigenteHasta()).contains(LocalDate.of(2025, 12, 31));
    assertThat(vencida.aplicaDesde()).contains(LocalDate.of(2020, 1, 1));
  }

  @Test
  void uc06_ext5b_lasQueYaNoRigenNoSeMezclanConLasVigentesNiSiElTextoLasComparte() {
    ReglaId regla = idDeLaReglaDeCreditos();
    laReglaYaNoRige(regla, LocalDate.of(2025, 12, 31));

    assertThat(buscar(BusquedaDeReglas.de("créditos", null, EstadoRegla.YA_NO_RIGE)))
        .extracting(ReglaVista::estado)
        .containsOnly(EstadoRegla.YA_NO_RIGE);
    assertThat(buscar(BusquedaDeReglas.de("créditos", null, EstadoRegla.VIGENTE)))
        .extracting(ReglaVista::id)
        .doesNotContain(regla);
  }

  @Test
  void uc06_ext5b_laPaginaMuestraLaReglaQueYaNoRigeConSuSenalYSuVigencia() throws Exception {
    laReglaYaNoRige(idDeLaReglaDeCreditos(), LocalDate.of(2025, 12, 31));

    String pagina =
        mockMvc
            .perform(get("/programas/" + sistemas().valor() + "/reglas?estado=YA_NO_RIGE"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(CIENTO_SESENTA_Y_CINCO)))
            .andExpect(content().string(containsString("<em>ya no rige</em>")))
            .andExpect(content().string(containsString("hasta 2025-12-31")))
            .andReturn()
            .getResponse()
            .getContentAsString();

    int inicio = pagina.lastIndexOf("<li>", pagina.indexOf(CIENTO_SESENTA_Y_CINCO));
    int fin = pagina.indexOf("</li>", inicio);
    LOG.info("Regla que ya no rige, como la muestra la página: {}", pagina.substring(inicio, fin));
  }

  @Test
  void uc06_ext5b_laVigenciaDeLaReglaSinVigenciaPropiaEsLaDeSuNorma() throws Exception {
    ReglaId regla = idDeLaReglaDeCreditos();
    laReglaYaNoRigePorqueSuNormaVencio(regla, LocalDate.of(2025, 12, 31));

    ReglaVista vencida = buscar(BusquedaDeReglas.de(null, null, EstadoRegla.YA_NO_RIGE)).getFirst();

    assertThat(vencida.id()).isEqualTo(regla);
    assertThat(vencida.vigenteHasta()).contains(LocalDate.of(2025, 12, 31));
    assertThat(vencida.aplicaDesde()).isPresent();
    mockMvc
        .perform(get("/programas/" + sistemas().valor() + "/reglas?estado=YA_NO_RIGE"))
        .andExpect(content().string(containsString("hasta 2025-12-31")))
        .andExpect(content().string(not(containsString("sin fechas registradas"))));
  }

  @Test
  void uc06_ext5b_laVigenciaHastaPropiaDeLaReglaGanaALaDeSuNorma() throws Exception {
    ReglaId regla = idDeLaReglaDeCreditos();
    laReglaYaNoRige(regla, LocalDate.of(2024, 6, 30));
    laNormaDeLaReglaVenceEl(regla, LocalDate.of(2025, 12, 31));

    ReglaVista vencida = buscar(BusquedaDeReglas.de(null, null, EstadoRegla.YA_NO_RIGE)).getFirst();

    assertThat(vencida.id()).isEqualTo(regla);
    assertThat(vencida.vigenteHasta()).contains(LocalDate.of(2024, 6, 30));
    mockMvc
        .perform(get("/programas/" + sistemas().valor() + "/reglas?estado=YA_NO_RIGE"))
        .andExpect(content().string(containsString("hasta 2024-06-30")))
        .andExpect(content().string(not(containsString("hasta 2025-12-31"))));
  }

  @Test
  void uc06_ext6c_laPaginaMarcaLaReglaInformativaYNoLaDelPlan() throws Exception {
    String reglas = "/programas/" + sistemas().valor() + "/reglas?texto=";

    mockMvc
        .perform(get(reglas + "modalidades de trabajo de grado"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("solo informativa: el plan no la aplica")));
    mockMvc
        .perform(get(reglas + "ciento sesenta y cinco"))
        .andExpect(content().string(containsString(CIENTO_SESENTA_Y_CINCO)))
        .andExpect(content().string(not(containsString("solo informativa"))));
  }

  @Test
  void uc06_paso5_elFiltroDeEstadoOfreceVigenteProgramadaYYaNoRige() throws Exception {
    mockMvc
        .perform(get("/programas/" + sistemas().valor() + "/reglas"))
        .andExpect(content().string(containsString("value=\"VIGENTE\"")))
        .andExpect(content().string(containsString("value=\"PROGRAMADA\"")))
        .andExpect(content().string(containsString("value=\"YA_NO_RIGE\"")))
        .andExpect(content().string(not(containsString("value=\"EN_REVISION\""))));
  }

  @Test
  void uc06_ext5a_sinResultadosLaPaginaSugiereRevisarYRecuerdaRegimenLegalSinIncidente()
      throws Exception {
    long antes = incidentes.count();

    mockMvc
        .perform(get("/programas/" + sistemas().valor() + "/reglas?texto=zzzzzz"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("No se encontraron reglas")))
        .andExpect(content().string(containsString("debe consultarse Régimen Legal")));

    assertThat(incidentes.count()).isEqualTo(antes);
  }
}
