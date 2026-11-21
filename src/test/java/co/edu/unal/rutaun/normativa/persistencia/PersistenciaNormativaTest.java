package co.edu.unal.rutaun.normativa.persistencia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;

import co.edu.unal.rutaun.soporte.PruebaDeIntegracion;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

@PruebaDeIntegracion
@Transactional
class PersistenciaNormativaTest {

  private static final Logger LOG = LoggerFactory.getLogger(PersistenciaNormativaTest.class);
  private static final LocalDate FECHA = LocalDate.of(2026, 10, 22);

  @Autowired private ProgramaRepository programas;
  @Autowired private MateriaRepository materias;
  @Autowired private NormaRepository normas;
  @Autowired private ReglaRepository reglas;
  @Autowired private VersionDelConsolidadoRepository versiones;
  @Autowired private ReglaEnVersionRepository reglasEnVersion;
  @Autowired private EntityManager em;

  private void vaciarMemoria() {
    em.flush();
    em.clear(); // obliga a que la lectura venga de la base de datos
  }

  private ProgramaEntity nuevoPrograma() {
    return programas.save(
        new ProgramaEntity(
            UUID.randomUUID(), "Ingenieria de Sistemas", "2879", "Ingenieria", "ING"));
  }

  private static NormaEntity entidadNorma(
      String tipo,
      String numero,
      int anio,
      String organoNombre,
      String organoNivel,
      String organoFacultadCodigo,
      String organoClave) {
    return new NormaEntity(
        UUID.randomUUID(),
        tipo,
        numero,
        anio,
        organoNombre,
        organoNivel,
        organoFacultadCodigo,
        organoClave,
        "BASE",
        LocalDate.of(2022, 1, 1),
        LocalDate.of(2022, 2, 1),
        null,
        null,
        "PROGRAMAS",
        "{}");
  }

  private NormaEntity nuevaNorma() {
    return normas.save(
        entidadNorma(
            "ACUERDO", "003-A", 2022, "Consejo Superior", "NACIONAL", null, "CONSEJO SUPERIOR"));
  }

  private VersionEntity nuevaVersion(UUID programaId, int numero) {
    return new VersionEntity(
        UUID.randomUUID(), programaId, numero, FECHA, "INCORPORACION", null, null, 1, "[]", null);
  }

  @Test
  void tt04_guardaYLeeUnPrograma() {
    ProgramaEntity programa = nuevoPrograma();
    vaciarMemoria();

    assertThat(programas.findById(programa.getId()))
        .get()
        .extracting(ProgramaEntity::getCodigoSia)
        .isEqualTo("2879");
  }

  @Test
  void sop03_dosProgramasConElMismoCodigoSiaFallan() {
    nuevoPrograma();

    assertThatThrownBy(
            () ->
                programas.saveAndFlush(
                    new ProgramaEntity(
                        UUID.randomUUID(), "Otro nombre", "2879", "Ingenieria", "ING")))
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessageContaining("UQ_PROGRAMA_CODIGO_SIA");
  }

  @Test
  void tt04_guardaYLeeUnaMateria() {
    materias.save(new MateriaEntity("2015714", 3));
    vaciarMemoria();

    assertThat(materias.findById("2015714"))
        .get()
        .extracting(MateriaEntity::getCreditos)
        .isEqualTo(3);
  }

  @Test
  void tt04_guardaYLeeUnaNorma() {
    NormaEntity norma = nuevaNorma();
    vaciarMemoria();

    assertThat(normas.findById(norma.getId()))
        .get()
        .extracting(NormaEntity::getNumero)
        .isEqualTo("003-A");
  }

  @Test
  void tt04_guardaYLeeUnaReglaConSusDatosJson() {
    NormaEntity norma = nuevaNorma();
    String json = "{\"materia\":\"2015714\",\"creditos\":3}";
    ReglaEntity regla =
        reglas.save(
            new ReglaEntity(
                UUID.randomUUID(),
                norma.getId(),
                "MATERIA",
                "Calculo diferencial",
                "La materia vale 3 creditos.",
                "Articulo 5",
                null,
                null,
                "DEL_PLAN",
                null,
                null,
                null,
                json));
    vaciarMemoria();

    assertThat(reglas.findById(regla.getId()))
        .get()
        .extracting(ReglaEntity::getDatos)
        .isEqualTo(json);
  }

  @Test
  void uc07_ext6b_unaAfectacionAUnaReglaInexistenteFalla() {
    NormaEntity norma = nuevaNorma();

    assertThatThrownBy(
            () ->
                reglas.saveAndFlush(
                    new ReglaEntity(
                        UUID.randomUUID(),
                        norma.getId(),
                        "OTRA",
                        "Regla que afecta",
                        "Modifica una regla inexistente.",
                        "Articulo 9",
                        null,
                        null,
                        "INFORMATIVA",
                        "MODIFICA",
                        UUID.randomUUID(),
                        null,
                        null)))
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessageContaining("FK_REGLA_AFECTADA");
  }

  @Test
  void tt04_unaAfectacionAUnaReglaGuardadaSeAcepta() {
    NormaEntity norma = nuevaNorma();
    ReglaEntity afectada =
        reglas.saveAndFlush(
            new ReglaEntity(
                UUID.randomUUID(),
                norma.getId(),
                "OTRA",
                "Regla afectada",
                "Regla existente.",
                "Articulo 1",
                null,
                null,
                "INFORMATIVA",
                null,
                null,
                null,
                null));
    ReglaEntity afectante =
        reglas.save(
            new ReglaEntity(
                UUID.randomUUID(),
                norma.getId(),
                "OTRA",
                "Regla que afecta",
                "Modifica la regla anterior.",
                "Articulo 2",
                null,
                null,
                "INFORMATIVA",
                "MODIFICA",
                afectada.getId(),
                null,
                null));
    vaciarMemoria();

    assertThat(reglas.findById(afectante.getId()))
        .get()
        .extracting(ReglaEntity::getReglaAfectadaId)
        .isEqualTo(afectada.getId());
  }

  @Test
  void tt04_guardaYLeeUnaVersionYSusReglas() {
    ProgramaEntity programa = nuevoPrograma();
    NormaEntity norma = nuevaNorma();
    ReglaEntity regla =
        reglas.save(
            new ReglaEntity(
                UUID.randomUUID(),
                norma.getId(),
                "OTRA",
                "General",
                "Regla informativa.",
                "Articulo 1",
                null,
                null,
                "INFORMATIVA",
                null,
                null,
                null,
                null));
    VersionEntity version = versiones.save(nuevaVersion(programa.getId(), 1));
    ReglaEnVersionEntity enVersion =
        reglasEnVersion.save(
            new ReglaEnVersionEntity(UUID.randomUUID(), version.getId(), regla.getId(), "VIGENTE"));
    vaciarMemoria();

    assertThat(reglasEnVersion.findById(enVersion.getId()))
        .get()
        .extracting(ReglaEnVersionEntity::getEstado)
        .isEqualTo("VIGENTE");
    assertThat(versiones.findById(version.getId()))
        .get()
        .extracting(VersionEntity::getNumero)
        .isEqualTo(1);
  }

  @Test
  void tt04_unaEntidadLeidaYaNoEsNueva() {
    ProgramaEntity programa = nuevoPrograma();
    vaciarMemoria();

    ProgramaEntity leida = programas.findById(programa.getId()).orElseThrow();

    assertThat(leida.isNew()).isFalse();
  }

  @Test
  void tt04_unaEntidadRecienCreadaEsNuevaYAlGuardarseDejaDeSerlo() {
    ProgramaEntity programa =
        new ProgramaEntity(UUID.randomUUID(), "Ingenieria Civil", "2501", "Ingenieria", "ING");

    assertThat(programa.isNew()).isTrue();
    programas.saveAndFlush(programa);
    assertThat(programa.isNew()).isFalse();
  }

  @Test
  void tt04_guardarComoNuevaUnaMateriaConUnCodigoExistenteFalla() {
    materias.saveAndFlush(new MateriaEntity("2015714", 3));
    em.clear();

    assertThatThrownBy(() -> materias.saveAndFlush(new MateriaEntity("2015714", 4)))
        .isInstanceOf(DataIntegrityViolationException.class);

    em.clear();
    assertThat(materias.findById("2015714"))
        .get()
        .extracting(MateriaEntity::getCreditos)
        .isEqualTo(3);
  }

  @Test
  void tt04_guardarComoNuevaUnaVersionConUnIdExistenteFalla() {
    ProgramaEntity programa = nuevoPrograma();
    VersionEntity original = nuevaVersion(programa.getId(), 1);
    versiones.saveAndFlush(original);
    vaciarMemoria();

    VersionEntity repetida =
        new VersionEntity(
            original.getId(),
            original.getProgramaId(),
            7,
            original.getFecha(),
            "OTRO",
            null,
            null,
            1,
            "[]",
            null);

    Throwable error = catchThrowable(() -> versiones.saveAndFlush(repetida));

    assertThat(error).isInstanceOf(DataIntegrityViolationException.class);
    em.clear();
    VersionEntity quedo = versiones.findById(original.getId()).orElseThrow();
    LOG.info(
        "Guardar una versión nueva con un id repetido falló con {}; la fila quedó con número {} y"
            + " motivo {}",
        error.getClass().getSimpleName(),
        quedo.getNumero(),
        quedo.getMotivo());
    assertThat(quedo.getNumero()).isEqualTo(1);
    assertThat(quedo.getMotivo()).isEqualTo("INCORPORACION");
    assertThat(versiones.count()).isEqualTo(1);
  }

  @Test
  void tt04_guardarComoNuevaUnaNormaConUnIdExistenteFalla() {
    NormaEntity original = nuevaNorma();
    vaciarMemoria();

    NormaEntity repetida =
        new NormaEntity(
            original.getId(),
            "ACUERDO",
            "007-B",
            2023,
            "Consejo Superior",
            "NACIONAL",
            null,
            "CONSEJO SUPERIOR",
            "BASE",
            LocalDate.of(2023, 1, 1),
            LocalDate.of(2023, 2, 1),
            null,
            null,
            "PROGRAMAS",
            "{}");

    assertThatThrownBy(() -> normas.saveAndFlush(repetida))
        .isInstanceOf(DataIntegrityViolationException.class);

    em.clear();
    assertThat(normas.findById(original.getId()))
        .get()
        .extracting(NormaEntity::getNumero)
        .isEqualTo("003-A");
  }

  @Test
  void tt04_laBaseRechazaUnaIdentificacionRepetidaYUnOrganoIncoherente() {
    nuevaNorma();

    assertThatThrownBy(
            () ->
                normas.saveAndFlush(
                    entidadNorma(
                        "ACUERDO",
                        "003-A",
                        2022,
                        "Consejo Superior",
                        "NACIONAL",
                        null,
                        "CONSEJO SUPERIOR")))
        .as("misma identificación")
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessageContaining("UQ_NORMA_IDENTIFICACION");
  }

  @Test
  void tt04_laBaseRechazaUnOrganoDeFacultadSinCodigo() {
    assertThatThrownBy(
            () ->
                normas.saveAndFlush(
                    entidadNorma(
                        "ACUERDO",
                        "010",
                        2022,
                        "Consejo de Facultad",
                        "FACULTAD",
                        null,
                        "CONSEJO DE FACULTAD")))
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessageContaining("CK_NORMA_ORGANO_FACULTAD");
  }

  @Test
  void tt04_laBaseRechazaUnCodigoDeFacultadConOtroNivel() {
    assertThatThrownBy(
            () ->
                normas.saveAndFlush(
                    entidadNorma(
                        "ACUERDO",
                        "011",
                        2022,
                        "Consejo Académico",
                        "NACIONAL",
                        "ING",
                        "CONSEJO ACADEMICO|ING")))
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessageContaining("CK_NORMA_ORGANO_FACULTAD");
  }

  @Test
  void tt04_laBaseRechazaUnNivelDeOrganoDesconocido() {
    assertThatThrownBy(
            () ->
                normas.saveAndFlush(
                    entidadNorma("ACUERDO", "012", 2022, "Consejo", "MUNDIAL", null, "CONSEJO")))
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessageContaining("CK_NORMA_ORGANO_NIVEL");
  }

  @ParameterizedTest(name = "{0} con día {1}")
  @MethodSource("diasQueNoCuadranConElMotivo")
  void tt04_elDiaAutomaticoDebeCoincidirConElMotivo(String motivo, LocalDate diaAutomatico) {
    ProgramaEntity programa = nuevoPrograma();
    VersionEntity version =
        new VersionEntity(
            UUID.randomUUID(),
            programa.getId(),
            1,
            FECHA,
            motivo,
            null,
            null,
            1,
            "[]",
            diaAutomatico);

    assertThatThrownBy(() -> versiones.saveAndFlush(version))
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessageContaining("CK_VERSION_DIA_AUTOMATICO");
  }

  static Stream<Arguments> diasQueNoCuadranConElMotivo() {
    return Stream.of(
        Arguments.of("AUTOMATICO_POR_FECHA", null),
        Arguments.of("AUTOMATICO_POR_FECHA", FECHA.minusDays(1)),
        Arguments.of("POR_CORRECCION", FECHA));
  }

  @Test
  void tt04_dosVersionesDelMismoProgramaConElMismoNumeroFallan() {
    ProgramaEntity programa = nuevoPrograma();
    versiones.saveAndFlush(nuevaVersion(programa.getId(), 1));

    assertThatThrownBy(() -> versiones.saveAndFlush(nuevaVersion(programa.getId(), 1)))
        .isInstanceOf(DataIntegrityViolationException.class);
  }
}
