package co.edu.unal.rutaun.normativa.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.unal.rutaun.compartido.EstadoRegla;
import co.edu.unal.rutaun.compartido.ProgramaId;
import co.edu.unal.rutaun.compartido.ReglaId;
import co.edu.unal.rutaun.normativa.ConsultaNormativaFacade;
import co.edu.unal.rutaun.normativa.EslabonVista;
import co.edu.unal.rutaun.normativa.ReglaCompletaVista;
import co.edu.unal.rutaun.normativa.ReglaVista;
import co.edu.unal.rutaun.normativa.TipoDeCambio;
import co.edu.unal.rutaun.normativa.persistencia.ProgramaRepository;
import co.edu.unal.rutaun.soporte.PruebaDeIntegracion;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
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
 * UC06 paso 8 con la semilla real (UC06-HU03, #36): abre una regla completa con la fachada real y
 * por la página, y comprueba la cadena de normas cuando otra norma la afecta, se abra la regla que
 * se abra. La semilla no trae afectaciones, así que la prueba inserta las que necesita.
 *
 * <p>Lleva {@code @Transactional}: lo que carga se deshace al terminar.
 */
@PruebaDeIntegracion
@AutoConfigureMockMvc
@Transactional
class ReglaCompletaIntegracionTest {

  private static final List<String> ACUERDOS =
      List.of(
          "semilla/acuerdo-003a-2022.json",
          "semilla/acuerdo-008-2008.json",
          "semilla/acuerdo-11-2023.json");

  private static final String CIENTO_SESENTA_Y_CINCO = "ciento sesenta y cinco (165)";

  @Autowired private ImportacionDeProgramasService importacionDeProgramas;
  @Autowired private ImportacionDeNormasService importacionDeNormas;
  @Autowired private ProgramaRepository programas;
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

  private ReglaId idDeLaReglaDeCreditos() {
    return consulta
        .consolidadoVigente(sistemas())
        .orElseThrow()
        .reglasPorCategoria()
        .values()
        .stream()
        .flatMap(List::stream)
        .filter(regla -> regla.enunciado().contains(CIENTO_SESENTA_Y_CINCO))
        .map(ReglaVista::id)
        .findFirst()
        .orElseThrow();
  }

  /**
   * Inserta una norma de 2030 y una regla suya que modifica la de créditos, con el estado dado en
   * la versión 3. La regla nueva copia los datos de la original para que sea una fila válida.
   */
  private void unaNormaPosteriorModificaLaRegla(ReglaId original, String estadoDeLaNueva) {
    unaNormaModifica(original, "999", "2030-01-01", estadoDeLaNueva);
  }

  /**
   * Inserta la norma con ese número y esa fecha de expedición y una regla suya que modifica a la
   * {@code afectada}. Le da el estado dado en la versión 3; sin estado, la deja fuera de la
   * versión, como una modificatoria que no comprende al programa. La regla nueva copia los datos de
   * la afectada para que sea una fila válida. Devuelve la regla nueva.
   */
  private ReglaId unaNormaModifica(
      ReglaId afectada, String numero, String expedicion, String estadoDeLaNueva) {
    entityManager.flush();
    UUID norma = UUID.randomUUID();
    UUID regla = UUID.randomUUID();
    jdbc.update(
        "INSERT INTO norma (id, tipo, numero, anio, organo_nombre, organo_nivel,"
            + " organo_facultad_codigo, organo_clave, tipo_de_efecto, fecha_expedicion,"
            + " aplica_desde, vigente_hasta, enlace_regimen_legal, tipo_alcance, alcance_detalle)"
            + " SELECT CAST(? AS UUID), tipo, CAST(? AS VARCHAR(50)), 2030, organo_nombre,"
            + " organo_nivel, organo_facultad_codigo, organo_clave, tipo_de_efecto,"
            + " CAST(? AS DATE), CAST(? AS DATE), NULL, NULL, tipo_alcance, alcance_detalle"
            + " FROM norma WHERE numero = '008'",
        norma,
        numero,
        expedicion,
        expedicion);
    jdbc.update(
        "INSERT INTO regla (id, norma_id, categoria, objeto, enunciado, fuente, vigente_desde,"
            + " vigente_hasta, clasificacion, tipo_afectacion, regla_afectada_id,"
            + " articulo_afectado, datos)"
            + " SELECT CAST(? AS UUID), CAST(? AS UUID), categoria, objeto, enunciado,"
            + " 'art. 9', NULL, NULL, clasificacion, 'MODIFICA', id, NULL, datos"
            + " FROM regla WHERE id = ?",
        regla,
        norma,
        afectada.valor());
    if (estadoDeLaNueva != null) {
      jdbc.update(
          "INSERT INTO regla_en_version (id, version_id, regla_id, estado)"
              + " SELECT CAST(? AS UUID), id, CAST(? AS UUID), CAST(? AS VARCHAR(20))"
              + " FROM version_consolidado WHERE programa_id = ? AND numero = 3",
          UUID.randomUUID(),
          regla,
          estadoDeLaNueva,
          sistemas().valor());
    }
    entityManager.flush();
    entityManager.clear();
    return new ReglaId(regla);
  }

  private void cambiarElEstadoDeLaOriginal(ReglaId original, String estado) {
    entityManager.flush();
    jdbc.update(
        "UPDATE regla_en_version SET estado = ? WHERE regla_id = ? AND version_id ="
            + " (SELECT id FROM version_consolidado WHERE programa_id = ? AND numero = 3)",
        estado,
        original.valor(),
        sistemas().valor());
    entityManager.flush();
    entityManager.clear();
  }

  @Test
  void uc06_paso8_unaReglaSinAfectacionesMuestraSuFuenteSuVigenciaYSuAlcance() {
    ReglaCompletaVista regla = consulta.regla(sistemas(), idDeLaReglaDeCreditos()).orElseThrow();

    assertThat(regla.enunciado()).contains(CIENTO_SESENTA_Y_CINCO);
    assertThat(regla.estado()).isEqualTo(EstadoRegla.VIGENTE);
    assertThat(regla.fuente()).isPresent();
    assertThat(regla.fuente().orElseThrow().articulo()).isNotBlank();
    assertThat(regla.aplicaDesde()).isPresent();
    assertThat(regla.vigenteHasta()).isEmpty();
    assertThat(regla.alcance()).isPresent();
    assertThat(regla.detalleDelAlcance()).isPresent();
    assertThat(regla.detalleDelAlcance().orElseThrow())
        .contains("Ingeniería de Sistemas y Computación")
        .doesNotContain("{")
        .doesNotContain("facultades");
    assertThat(regla.temporal()).isFalse();
  }

  @Test
  void uc06_ext8a_unaReglaSinAfectacionesTieneUnSoloPasoQueRige() {
    ReglaCompletaVista regla = consulta.regla(sistemas(), idDeLaReglaDeCreditos()).orElseThrow();

    assertThat(regla.tieneCadena()).isFalse();
    assertThat(regla.cadena()).hasSize(1);
    assertThat(regla.cadena().getFirst().cambio()).isEmpty();
    assertThat(regla.cadena().getFirst().rige()).isTrue();
  }

  @Test
  void uc06_ext8a_conUnaNormaPosteriorLaCadenaVaDeLaMasAntiguaALaMasReciente() {
    ReglaId original = idDeLaReglaDeCreditos();
    unaNormaPosteriorModificaLaRegla(original, "PROGRAMADA");

    ReglaCompletaVista regla = consulta.regla(sistemas(), original).orElseThrow();

    assertThat(regla.tieneCadena()).isTrue();
    List<EslabonVista> cadena = regla.cadena();
    assertThat(cadena).hasSize(2);
    assertThat(cadena.get(0).cambio()).isEmpty();
    assertThat(cadena.get(1).cambio()).contains(TipoDeCambio.MODIFICA);
    assertThat(cadena.get(1).fuente().norma().numero()).isEqualTo("999");
    assertThat(cadena.get(1).fuente().articulo()).isEqualTo("art. 9");
    assertThat(cadena.get(0).fechaExpedicion()).isBefore(cadena.get(1).fechaExpedicion());
  }

  @Test
  void uc06_ext8a_laCadenaMarcaComoVigenteLaReglaQueEstaVigenteEnElConsolidado() {
    ReglaId original = idDeLaReglaDeCreditos();
    unaNormaPosteriorModificaLaRegla(original, "PROGRAMADA");

    List<EslabonVista> conLaOriginalVigente =
        consulta.regla(sistemas(), original).orElseThrow().cadena();

    assertThat(conLaOriginalVigente).extracting(EslabonVista::rige).containsExactly(true, false);

    cambiarElEstadoDeLaOriginal(original, "EN_REVISION");
    jdbc.update(
        "UPDATE regla_en_version SET estado = 'VIGENTE' WHERE regla_id IN"
            + " (SELECT id FROM regla WHERE regla_afectada_id = ?)",
        original.valor());
    entityManager.clear();

    List<EslabonVista> conLaNuevaVigente =
        consulta.regla(sistemas(), original).orElseThrow().cadena();

    assertThat(conLaNuevaVigente).extracting(EslabonVista::rige).containsExactly(false, true);
  }

  @Test
  void uc06_ext8a_unaReglaQueYaNoRigeSeAbreConEseEstadoYMuestraQuienLaReemplaza() {
    ReglaId original = idDeLaReglaDeCreditos();
    unaNormaModifica(original, "999", "2030-01-01", "VIGENTE");
    cambiarElEstadoDeLaOriginal(original, "YA_NO_RIGE");

    ReglaCompletaVista regla = consulta.regla(sistemas(), original).orElseThrow();

    assertThat(regla.estado()).isEqualTo(EstadoRegla.YA_NO_RIGE);
    assertThat(regla.cadena()).extracting(EslabonVista::rige).containsExactly(false, true);
    assertThat(regla.cadena().get(1).fuente().norma().numero()).isEqualTo("999");
  }

  @Test
  void uc06_ext8a_desdeLaReglaQueRigeLaCadenaIncluyeLaOriginalQueYaNoRige() {
    ReglaId original = idDeLaReglaDeCreditos();
    ReglaId nueva = unaNormaModifica(original, "999", "2030-01-01", "VIGENTE");
    cambiarElEstadoDeLaOriginal(original, "YA_NO_RIGE");

    ReglaCompletaVista regla = consulta.regla(sistemas(), nueva).orElseThrow();

    assertThat(regla.estado()).isEqualTo(EstadoRegla.VIGENTE);
    assertThat(regla.tieneCadena()).isTrue();
    assertThat(regla.cadena())
        .extracting(eslabon -> eslabon.cambio().orElse(null))
        .containsExactly(null, TipoDeCambio.MODIFICA);
    assertThat(regla.cadena()).extracting(EslabonVista::rige).containsExactly(false, true);
  }

  @Test
  void uc06_ext8a_unaCadenaDeTresNivelesSeVeIgualDesdeCadaRegla() {
    ReglaId a = idDeLaReglaDeCreditos();
    ReglaId b = unaNormaModifica(a, "999", "2030-01-01", "YA_NO_RIGE");
    ReglaId c = unaNormaModifica(b, "998", "2031-01-01", "VIGENTE");
    cambiarElEstadoDeLaOriginal(a, "YA_NO_RIGE");

    for (ReglaId abierta : List.of(a, b, c)) {
      List<EslabonVista> cadena = consulta.regla(sistemas(), abierta).orElseThrow().cadena();

      assertThat(cadena).hasSize(3);
      assertThat(cadena)
          .extracting(eslabon -> eslabon.fuente().norma().numero())
          .endsWith("999", "998");
      assertThat(cadena).extracting(EslabonVista::rige).containsExactly(false, false, true);
    }
  }

  @Test
  void uc06_ext8a_unaModificatoriaQueNoEstaEnLaVersionNoEntraEnLaCadena() {
    ReglaId original = idDeLaReglaDeCreditos();
    ReglaId fuera = unaNormaModifica(original, "999", "2030-01-01", null);

    assertThat(consulta.regla(sistemas(), original).orElseThrow().cadena()).hasSize(1);
    assertThat(consulta.regla(sistemas(), fuera)).isEmpty();
  }

  private ReglaId idDeLaReglaDeAjustes() throws IOException {
    try (InputStream entrada =
        new ClassPathResource("semilla/datos-de-prueba-vigentes.json").getInputStream()) {
      importacionDeNormas.importar(entrada);
    }
    return consulta
        .consolidadoVigente(sistemas())
        .orElseThrow()
        .reglasPorCategoria()
        .values()
        .stream()
        .flatMap(List::stream)
        .filter(regla -> regla.enunciado().contains("ajustes de matrícula"))
        .map(ReglaVista::id)
        .findFirst()
        .orElseThrow();
  }

  @Test
  void uc06_paso8_laVigenciaHastaPropiaDeLaReglaGanaALaDeSuNorma() throws IOException {
    ReglaCompletaVista regla = consulta.regla(sistemas(), idDeLaReglaDeAjustes()).orElseThrow();

    assertThat(regla.vigenteHasta()).contains(LocalDate.of(2099, 12, 31));
    assertThat(regla.temporal()).isTrue();
  }

  @Test
  void uc06_paso8_laFechaDeAplicacionPropiaDeLaReglaGanaALaDeSuNorma() throws IOException {
    ReglaId ajustes = idDeLaReglaDeAjustes();
    entityManager.flush();
    jdbc.update("UPDATE regla SET vigente_desde = DATE '2026-03-01' WHERE id = ?", ajustes.valor());
    entityManager.clear();

    ReglaCompletaVista regla = consulta.regla(sistemas(), ajustes).orElseThrow();

    assertThat(regla.aplicaDesde()).contains(LocalDate.of(2026, 3, 1));
  }

  @Test
  void uc06_paso8_unaReglaOUnProgramaQueNoExistenNoSeAbren() {
    ReglaId original = idDeLaReglaDeCreditos();

    assertThat(consulta.regla(sistemas(), ReglaId.nuevo())).isEmpty();
    assertThat(consulta.regla(new ProgramaId(UUID.randomUUID()), original)).isEmpty();
  }

  @Test
  void uc06_paso8_laPaginaDeLaReglaMuestraLaCadenaYLaNotaDeLaFuenteOficial() throws Exception {
    ReglaId original = idDeLaReglaDeCreditos();
    unaNormaPosteriorModificaLaRegla(original, "PROGRAMADA");

    mockMvc
        .perform(get("/programas/" + sistemas().valor() + "/reglas/" + original.valor()))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString(CIENTO_SESENTA_Y_CINCO)))
        .andExpect(content().string(not(containsString("\"facultades\""))))
        .andExpect(content().string(containsString("Normas que han afectado esta regla")))
        .andExpect(
            content()
                .string(stringContainsInOrder("Expide la regla:", "La modifica:", "999 de 2030")))
        .andExpect(content().string(containsString("RutaUN no es la fuente oficial")));
  }

  @Test
  void uc06_paso8_laPaginaDeLasReglasEnlazaCadaReglaConSuDetalle() throws Exception {
    ReglaId original = idDeLaReglaDeCreditos();

    mockMvc
        .perform(get("/programas/" + sistemas().valor() + "/reglas"))
        .andExpect(status().isOk())
        .andExpect(
            content()
                .string(
                    containsString(
                        "/programas/" + sistemas().valor() + "/reglas/" + original.valor())));
  }

  @Test
  void uc06_paso8_unaReglaQueNoSeMuestraResponde404() throws Exception {
    mockMvc
        .perform(get("/programas/" + sistemas().valor() + "/reglas/" + UUID.randomUUID()))
        .andExpect(status().isNotFound());
  }
}
