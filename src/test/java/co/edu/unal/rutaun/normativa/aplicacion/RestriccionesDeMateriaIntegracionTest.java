package co.edu.unal.rutaun.normativa.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.compartido.EstadoRegla;
import co.edu.unal.rutaun.compartido.ProgramaId;
import co.edu.unal.rutaun.normativa.ConsultaNormativaFacade;
import co.edu.unal.rutaun.normativa.ExpresionVista;
import co.edu.unal.rutaun.normativa.RestriccionVista;
import co.edu.unal.rutaun.normativa.RestriccionesDeMateriaVista;
import co.edu.unal.rutaun.normativa.TipoDeRestriccion;
import co.edu.unal.rutaun.normativa.TipoNorma;
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
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/**
 * UC05 pasos 2 a 4 con la semilla real (UC05-HU01, #27): lo que una materia de Ingeniería de
 * Sistemas exige para cursarla, con la fachada real. Usa las materias de la malla del Acuerdo 11 de
 * 2023 y, para el correquisito, la norma de datos de prueba.
 *
 * <p>Lleva {@code @Transactional}: lo que carga se deshace al terminar.
 */
@PruebaDeIntegracion
@Transactional
class RestriccionesDeMateriaIntegracionTest {

  private static final List<String> NORMAS =
      List.of(
          "semilla/acuerdo-003a-2022.json",
          "semilla/acuerdo-008-2008.json",
          "semilla/acuerdo-11-2023.json",
          "semilla/datos-de-prueba-vigentes.json");

  private static final CodigoMateria ESTRUCTURAS_DE_DATOS = new CodigoMateria("2016699");
  private static final CodigoMateria CALCULO_INTEGRAL = new CodigoMateria("1000005");
  private static final CodigoMateria ELECTRICIDAD_Y_MAGNETISMO = new CodigoMateria("1000017");
  private static final CodigoMateria CALCULO_DIFERENCIAL = new CodigoMateria("1000004");
  private static final CodigoMateria COMUNICACIONES = new CodigoMateria("2016492");
  private static final CodigoMateria ELECTRONICA_DIGITAL_I = new CodigoMateria("2016498");
  private static final CodigoMateria TRABAJO_DE_GRADO = new CodigoMateria("2025974");
  private static final CodigoMateria PROGRAMACION_DE_COMPUTADORES = new CodigoMateria("2015734");

  @Autowired private ImportacionDeProgramasService importacionDeProgramas;
  @Autowired private ImportacionDeNormasService importacionDeNormas;
  @Autowired private ProgramaRepository programas;
  @Autowired private ConsultaNormativaFacade consulta;
  @Autowired private JdbcTemplate jdbc;
  @PersistenceContext private EntityManager entityManager;

  @BeforeEach
  void cargarLaSemilla() throws IOException {
    importacionDeProgramas.importar(
        new ClassPathResource("semilla/programas.json").getContentAsString(StandardCharsets.UTF_8));
    for (String archivo : NORMAS) {
      try (InputStream entrada = new ClassPathResource(archivo).getInputStream()) {
        importacionDeNormas.importar(entrada);
      }
    }
  }

  private ProgramaId sistemas() {
    return new ProgramaId(programas.findByCodigoSia("2879").orElseThrow().getId());
  }

  private RestriccionesDeMateriaVista restriccionesDe(CodigoMateria materia) {
    return consulta.restriccionesDe(sistemas(), materia).orElseThrow();
  }

  private static ExpresionVista.Materia materia(String codigo, String nombre) {
    return new ExpresionVista.Materia(new CodigoMateria(codigo), nombre);
  }

  @Test
  void uc05_paso4_unPrerrequisitoMuestraQueExigeConSuFuenteYSuVigencia() {
    RestriccionesDeMateriaVista vista = restriccionesDe(ESTRUCTURAS_DE_DATOS);

    assertThat(vista.nombre()).isEqualTo("Estructuras de Datos");
    assertThat(vista.version()).isPositive();
    assertThat(vista.restricciones()).hasSize(1);
    RestriccionVista restriccion = vista.restricciones().getFirst();
    assertThat(restriccion.tipo()).isEqualTo(TipoDeRestriccion.PRERREQUISITO);
    assertThat(restriccion.enunciado()).contains("Estructuras de Datos");
    assertThat(restriccion.exige())
        .isEqualTo(materia("2016375", "Programación Orientada a Objetos"));
    assertThat(restriccion.estado()).isEqualTo(EstadoRegla.VIGENTE);
    assertThat(restriccion.temporal()).isFalse();
    assertThat(restriccion.aplicaDesde()).contains(LocalDate.of(2023, 12, 12));
    assertThat(restriccion.vigenteHasta()).isEmpty();
    assertThat(restriccion.fuente()).isPresent();
    assertThat(restriccion.fuente().get().articulo()).isEqualTo("art. 2");
    assertThat(restriccion.fuente().get().norma().tipo()).isEqualTo(TipoNorma.ACUERDO);
    assertThat(restriccion.fuente().get().norma().numero()).isEqualTo("11");
    assertThat(restriccion.fuente().get().norma().anio()).isEqualTo(2023);
  }

  @Test
  void uc05_ext4c_unaMateriaConReglaDeHabilitacionLaMuestraYNoDiceQueNoTieneRestricciones() {
    RestriccionesDeMateriaVista vista = restriccionesDe(TRABAJO_DE_GRADO);

    assertThat(vista.sinRestricciones()).isFalse();
    assertThat(vista.restricciones()).hasSize(1);
    RestriccionVista restriccion = vista.restricciones().getFirst();
    assertThat(restriccion.tipo()).isEqualTo(TipoDeRestriccion.HABILITACION);
    assertThat(restriccion.exige().redaccion()).contains("haber aprobado 60 créditos");
    assertThat(restriccion.estado()).isEqualTo(EstadoRegla.VIGENTE);
    assertThat(restriccion.fuente()).isPresent();
    assertThat(restriccion.fuente().get().articulo()).isEqualTo("art. 2");
    assertThat(restriccion.aplicaDesde()).contains(LocalDate.of(2023, 12, 12));
  }

  @Test
  void uc05_ext4k_unaMateriaCuyoPrerrequisitoEsDeOtroProgramaNoSeDaPorSinRestricciones() {
    for (CodigoMateria materia : List.of(COMUNICACIONES, ELECTRONICA_DIGITAL_I)) {
      RestriccionesDeMateriaVista vista = restriccionesDe(materia);

      assertThat(vista.noCursable()).as(materia.valor()).isTrue();
      assertThat(vista.sinRestricciones()).as(materia.valor()).isFalse();
    }
  }

  @Test
  void uc05_ext4k_unaMateriaCuyoPrerrequisitoEsDeNivelacionSeCursaYNoExigeNada() {
    RestriccionesDeMateriaVista vista = restriccionesDe(CALCULO_DIFERENCIAL);

    assertThat(vista.noCursable()).isFalse();
    assertThat(vista.sinRestricciones()).isTrue();
  }

  @Test
  void uc05_ext4a_unPrerrequisitoConAlternativasLasMuestraTodas() {
    RestriccionVista restriccion = restriccionesDe(CALCULO_INTEGRAL).restricciones().getFirst();

    assertThat(restriccion.exige())
        .isEqualTo(
            new ExpresionVista.AlgunaDe(
                List.of(
                    materia("1000004", "Cálculo Diferencial"),
                    materia("2016377", "Cálculo Diferencial en una Variable"))));
  }

  @Test
  void uc05_ext4a_unaAlternativaDentroDeUnaConjuncionConservaSuNivel() {
    RestriccionVista restriccion =
        restriccionesDe(ELECTRICIDAD_Y_MAGNETISMO).restricciones().getFirst();

    assertThat(restriccion.exige())
        .isEqualTo(
            new ExpresionVista.TodasLas(
                List.of(
                    materia("1000019", "Fundamentos de Mecánica"),
                    new ExpresionVista.AlgunaDe(
                        List.of(
                            materia("1000005", "Cálculo Integral"),
                            materia("2015556", "Cálculo integral en una variable"))))));
    assertThat(restriccion.exige().redaccion())
        .isEqualTo(
            "«Fundamentos de Mecánica» y («Cálculo Integral» o"
                + " «Cálculo integral en una variable»)");
  }

  @Test
  void uc05_paso4_unCorrequisitoSeMuestraComoCorrequisito() {
    RestriccionVista restriccion =
        restriccionesDe(PROGRAMACION_DE_COMPUTADORES).restricciones().getFirst();

    assertThat(restriccion.tipo()).isEqualTo(TipoDeRestriccion.CORREQUISITO);
    assertThat(restriccion.exige()).isInstanceOf(ExpresionVista.AlgunaDe.class);
    assertThat(restriccion.fuente().orElseThrow().norma().numero()).isEqualTo("901");
  }

  @Test
  void uc05_ext3a_unaMateriaSinRestriccionesDevuelveLaVistaSinRestricciones() {
    RestriccionesDeMateriaVista vista = restriccionesDe(new CodigoMateria("2016703"));

    assertThat(vista.sinRestricciones()).isTrue();
    assertThat(vista.restricciones()).isEmpty();
    assertThat(vista.nombre()).isNotBlank();
  }

  @Test
  void uc05_ext4k_unPrerrequisitoDeNivelacionNoSeMuestra() {
    RestriccionesDeMateriaVista vista = restriccionesDe(CALCULO_DIFERENCIAL);

    assertThat(vista.nombre()).isEqualTo("Cálculo Diferencial");
    assertThat(vista.restricciones()).isEmpty();
  }

  @Test
  void uc05_paso2_unaMateriaQueNoEstaEnLaMallaNoSeExplica() {
    assertThat(consulta.restriccionesDe(sistemas(), new CodigoMateria("9999999"))).isEmpty();
  }

  @Test
  void uc05_precondicion1_unProgramaSinConsolidadoNoSeExplica() {
    ProgramaId desconocido = new ProgramaId(UUID.randomUUID());

    assertThat(consulta.restriccionesDe(desconocido, ESTRUCTURAS_DE_DATOS)).isEmpty();
  }

  /**
   * Agrega a la última versión una regla programada que le da otro nombre a la materia, como haría
   * una norma que aún no rige.
   */
  private void unaNormaProgramadaLeCambiaElNombre(CodigoMateria materia, String nombre) {
    entityManager.flush();
    int copiadas =
        jdbc.update(
            "INSERT INTO regla (id, norma_id, categoria, objeto, enunciado, fuente, vigente_desde,"
                + " vigente_hasta, clasificacion, datos) SELECT RANDOM_UUID(), norma_id, categoria,"
                + " objeto, enunciado, fuente, vigente_desde, vigente_hasta, clasificacion,"
                + " REPLACE(datos, ?, ?) FROM regla WHERE categoria = 'MATERIA'"
                + " AND datos LIKE ?",
            "Estructuras de Datos",
            nombre,
            "%" + materia.valor() + "%");
    jdbc.update(
        "INSERT INTO regla_en_version (id, version_id, regla_id, estado) SELECT RANDOM_UUID(),"
            + " (SELECT id FROM version_consolidado WHERE programa_id = ? ORDER BY numero DESC"
            + " LIMIT 1), id, 'PROGRAMADA' FROM regla WHERE categoria = 'MATERIA'"
            + " AND datos LIKE ?",
        sistemas().valor(),
        "%" + nombre + "%");
    assertThat(copiadas).isPositive();
    entityManager.flush();
    entityManager.clear();
  }

  @Test
  void uc05_paso4_unaNormaProgramadaNoCambiaElNombreQueSeMuestra() {
    unaNormaProgramadaLeCambiaElNombre(ESTRUCTURAS_DE_DATOS, "Estructuras de Datos (futuro)");

    assertThat(restriccionesDe(ESTRUCTURAS_DE_DATOS).nombre()).isEqualTo("Estructuras de Datos");
  }

  private UUID prerrequisitoDe(CodigoMateria materia) {
    return jdbc.queryForObject(
        "SELECT id FROM regla WHERE categoria = 'PRERREQUISITO' AND objeto LIKE ?",
        UUID.class,
        materia.valor() + " %");
  }

  /** Cambia el estado de la regla en la última versión del programa. */
  private void laReglaPasaAEstado(UUID regla, EstadoRegla estado) {
    entityManager.flush();
    jdbc.update(
        "UPDATE regla_en_version SET estado = ? WHERE regla_id = ? AND version_id ="
            + " (SELECT id FROM version_consolidado WHERE programa_id = ? ORDER BY numero DESC"
            + " LIMIT 1)",
        estado.name(),
        regla,
        sistemas().valor());
    entityManager.flush();
    entityManager.clear();
  }

  /** Le da a la regla su propia vigencia; queda informativa, porque una temporal no es del plan. */
  private void laReglaTieneVigenciaPropia(UUID regla, LocalDate desde, LocalDate hasta) {
    entityManager.flush();
    jdbc.update(
        "UPDATE regla SET vigente_desde = ?, vigente_hasta = ?, clasificacion = 'INFORMATIVA'"
            + " WHERE id = ?",
        desde,
        hasta,
        regla);
    entityManager.flush();
    entityManager.clear();
  }

  /** Le pone fin de vigencia a la norma de la regla; la regla queda informativa. */
  private void laNormaDeLaReglaVenceEl(UUID regla, LocalDate hasta) {
    entityManager.flush();
    jdbc.update("UPDATE regla SET clasificacion = 'INFORMATIVA' WHERE id = ?", regla);
    jdbc.update(
        "UPDATE norma SET vigente_hasta = ? WHERE id = (SELECT norma_id FROM regla WHERE id = ?)",
        hasta,
        regla);
    entityManager.flush();
    entityManager.clear();
  }

  @Test
  void uc05_ge2_laVigenciaPropiaDeLaRestriccionGanaALaDeSuNorma() {
    UUID regla = prerrequisitoDe(ESTRUCTURAS_DE_DATOS);
    laReglaTieneVigenciaPropia(regla, LocalDate.of(2024, 2, 1), LocalDate.of(2030, 6, 30));
    laNormaDeLaReglaVenceEl(regla, LocalDate.of(2035, 12, 31));

    RestriccionVista restriccion = restriccionesDe(ESTRUCTURAS_DE_DATOS).restricciones().getFirst();

    assertThat(restriccion.aplicaDesde()).contains(LocalDate.of(2024, 2, 1));
    assertThat(restriccion.vigenteHasta()).contains(LocalDate.of(2030, 6, 30));
    assertThat(restriccion.temporal()).isTrue();
  }

  @Test
  void uc05_ge2_sinVigenciaPropiaLaRestriccionTomaElFinDeSuNorma() {
    laNormaDeLaReglaVenceEl(prerrequisitoDe(ESTRUCTURAS_DE_DATOS), LocalDate.of(2030, 6, 30));

    RestriccionVista restriccion = restriccionesDe(ESTRUCTURAS_DE_DATOS).restricciones().getFirst();

    assertThat(restriccion.vigenteHasta()).contains(LocalDate.of(2030, 6, 30));
    assertThat(restriccion.aplicaDesde()).contains(LocalDate.of(2023, 12, 12));
    assertThat(restriccion.temporal()).isTrue();
  }

  @Test
  void uc05_ext3a_unaRestriccionQueYaNoRigeNoSeMuestra() {
    laReglaPasaAEstado(prerrequisitoDe(ESTRUCTURAS_DE_DATOS), EstadoRegla.YA_NO_RIGE);

    assertThat(restriccionesDe(ESTRUCTURAS_DE_DATOS).restricciones()).isEmpty();
  }

  @Test
  void uc05_ge2_unaRestriccionProgramadaOEnRevisionSeMuestraConSuEstado() {
    UUID regla = prerrequisitoDe(ESTRUCTURAS_DE_DATOS);

    laReglaPasaAEstado(regla, EstadoRegla.PROGRAMADA);
    assertThat(restriccionesDe(ESTRUCTURAS_DE_DATOS).restricciones())
        .singleElement()
        .extracting(RestriccionVista::estado)
        .isEqualTo(EstadoRegla.PROGRAMADA);

    laReglaPasaAEstado(regla, EstadoRegla.EN_REVISION);
    assertThat(restriccionesDe(ESTRUCTURAS_DE_DATOS).restricciones())
        .singleElement()
        .extracting(RestriccionVista::estado)
        .isEqualTo(EstadoRegla.EN_REVISION);
  }
}
