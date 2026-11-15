package co.edu.unal.rutaun.normativa.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.unal.rutaun.compartido.EstadoRegla;
import co.edu.unal.rutaun.compartido.ProgramaId;
import co.edu.unal.rutaun.normativa.ConsolidadoVista;
import co.edu.unal.rutaun.normativa.ConsultaNormativaFacade;
import co.edu.unal.rutaun.normativa.ReglaVista;
import co.edu.unal.rutaun.normativa.persistencia.ProgramaJpaRepository;
import co.edu.unal.rutaun.soporte.PruebaDeIntegracion;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * UC06 paso 4 con la semilla real (UC06-HU01-T4, #132): carga los programas y los tres acuerdos de
 * {@code semilla/}, como el perfil {@code dev}, y consulta con la fachada real y por la página.
 *
 * <p>Lleva {@code @Transactional}: lo que carga se deshace al terminar y no queda en la base que
 * comparten las demás pruebas.
 */
@PruebaDeIntegracion
@AutoConfigureMockMvc
@Transactional
class ConsultaNormativaServiceIntegracionTest {

  /**
   * Los tres acuerdos de la semilla, por nombre: un patrón {@code semilla/*.json} también recogería
   * los archivos de prueba que hay en la misma carpeta.
   */
  private static final List<String> ACUERDOS =
      List.of(
          "semilla/acuerdo-003a-2022.json",
          "semilla/acuerdo-008-2008.json",
          "semilla/acuerdo-11-2023.json");

  @Autowired private ImportacionDeProgramasService importacionDeProgramas;
  @Autowired private ImportacionDeNormasService importacionDeNormas;
  @Autowired private ProgramaJpaRepository programas;
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

  private ConsolidadoVista consolidado() {
    return consulta.consolidadoVigente(sistemas()).orElseThrow();
  }

  @Test
  void uc06_paso4_conLaSemillaSeVenLasReglasDeLosTresAcuerdos() throws Exception {
    mockMvc
        .perform(get("/programas"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Ingeniería de Sistemas y Computación")));

    mockMvc
        .perform(get("/programas/" + sistemas().valor() + "/reglas"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Versión del consolidado: 3")))
        .andExpect(content().string(containsString("Componentes y créditos")))
        .andExpect(content().string(containsString("ciento sesenta y cinco (165) créditos")))
        .andExpect(content().string(containsString("Carga mínima o máxima por periodo")))
        .andExpect(content().string(containsString("diez (10) créditos como mínimo")))
        .andExpect(content().string(containsString("003-A de 2022")))
        .andExpect(content().string(containsString("008 de 2008")))
        .andExpect(content().string(containsString("11 de 2023")));
  }

  @Test
  void uc06_paso4_elConsolidadoVigenteEsLaVersionDeMayorNumero() {
    assertThat(consolidado().version()).isEqualTo(3);
  }

  @Test
  void uc06_paso4_noMuestraLasReglasQueYaNoRigen() {
    List<ReglaVista> todas =
        consolidado().reglasPorCategoria().values().stream().flatMap(List::stream).toList();

    assertThat(todas).isNotEmpty();
    assertThat(todas).extracting(ReglaVista::estado).doesNotContain(EstadoRegla.YA_NO_RIGE);
  }

  @Test
  void uc06_paso4_lasCategoriasVanEnElOrdenDelGlosario() {
    List<String> orden =
        List.of(
            "Componentes y créditos",
            "Materias",
            "Agrupaciones",
            "Prerrequisitos y habilitación",
            "Equivalencias",
            "Libre elección",
            "Carga mínima o máxima por periodo",
            "Requisitos de grado",
            "Otra");

    List<String> categorias = List.copyOf(consolidado().reglasPorCategoria().keySet());

    assertThat(categorias).isNotEmpty().isSubsetOf(orden);
    assertThat(categorias).isSortedAccordingTo(Comparator.comparingInt(orden::indexOf));
  }

  @Test
  void uc06_paso4_dentroDeUnaCategoriaVaPrimeroLaNormaMasAntigua() {
    ReglaVista primera =
        consolidado().reglasPorCategoria().get("Carga mínima o máxima por periodo").getFirst();

    assertThat(primera.fuente().orElseThrow().norma().numero()).isEqualTo("008");
  }

  @Test
  void uc06_paso4_cadaReglaCitaSuNormaYSuArticulo() {
    List<ReglaVista> todas =
        consolidado().reglasPorCategoria().values().stream().flatMap(List::stream).toList();

    assertThat(todas).allSatisfy(regla -> assertThat(regla.fuente()).isPresent());
  }

  @Test
  void uc06_paso2_unProgramaQueNoEstaCargadoNoTieneConsolidado() {
    ProgramaId desconocido = new ProgramaId(UUID.randomUUID());

    assertThat(consulta.programa(desconocido)).isEmpty();
    assertThat(consulta.consolidadoVigente(desconocido)).isEmpty();
  }

  private static final String CIENTO_SESENTA_Y_CINCO = "%ciento sesenta y cinco (165)%";

  private static List<ReglaVista> todas(ConsolidadoVista consolidado) {
    return consolidado.reglasPorCategoria().values().stream().flatMap(List::stream).toList();
  }

  /**
   * Cambia el estado de la regla en la versión 3 y deja que la fachada lea de nuevo. Primero
   * escribe en la base lo que la carga de la semilla dejó pendiente en Hibernate; si no, el UPDATE
   * no encontraría esas filas.
   */
  private void marcarLaRegla(String enunciadoComo, String estado) {
    entityManager.flush();
    jdbc.update(
        "UPDATE regla_en_version SET estado = ? WHERE version_id = "
            + "(SELECT id FROM version_consolidado WHERE programa_id = ? AND numero = 3) "
            + "AND regla_id IN (SELECT id FROM regla WHERE enunciado LIKE ?)",
        estado,
        sistemas().valor(),
        enunciadoComo);
    entityManager.flush();
    entityManager.clear();
  }

  @Test
  void uc06_paso4_unaReglaQueYaNoRigeNoSeMuestra() {
    int antes = todas(consolidado()).size();

    marcarLaRegla(CIENTO_SESENTA_Y_CINCO, "YA_NO_RIGE");

    List<ReglaVista> despues = todas(consolidado());
    assertThat(despues).hasSize(antes - 1);
    assertThat(despues)
        .extracting(ReglaVista::enunciado)
        .noneMatch(enunciado -> enunciado.contains("(165) créditos"));
  }

  @Test
  void uc06_paso4_unaReglaEnRevisionSeMuestraConSuEstado() {
    marcarLaRegla(CIENTO_SESENTA_Y_CINCO, "EN_REVISION");

    ReglaVista regla =
        todas(consolidado()).stream()
            .filter(r -> r.enunciado().contains("(165) créditos"))
            .findFirst()
            .orElseThrow();
    assertThat(regla.estado()).isEqualTo(EstadoRegla.EN_REVISION);
  }

  @Test
  void uc06_paso4_lasReglasDeUnaNormaConVigenciaHastaSeMarcanTemporales() {
    assertThat(todas(consolidado())).noneMatch(ReglaVista::temporal);

    entityManager.flush();
    jdbc.update("UPDATE norma SET vigente_hasta = DATE '2030-01-01' WHERE numero = '008'");
    entityManager.flush();
    entityManager.clear();

    List<ReglaVista> reglas = todas(consolidado());
    assertThat(reglas)
        .filteredOn(r -> r.fuente().orElseThrow().norma().numero().equals("008"))
        .isNotEmpty()
        .allMatch(ReglaVista::temporal);
    assertThat(reglas)
        .filteredOn(r -> !r.fuente().orElseThrow().norma().numero().equals("008"))
        .noneMatch(ReglaVista::temporal);
  }
}
