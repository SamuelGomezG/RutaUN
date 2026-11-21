package co.edu.unal.rutaun.normativa.persistencia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;

import co.edu.unal.rutaun.soporte.PruebaDeIntegracion;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@PruebaDeIntegracion
@Transactional
class PersistenciaNormativaTest {

  private static final Logger LOG = LoggerFactory.getLogger(PersistenciaNormativaTest.class);

  @Autowired private ProgramaRepository programas;
  @Autowired private MateriaRepository materias;
  @Autowired private NormaRepository normas;
  @Autowired private ReglaRepository reglas;
  @Autowired private ConsolidadoRepository consolidados;
  @Autowired private VersionDelConsolidadoRepository versiones;
  @Autowired private ReglaEnVersionRepository reglasEnVersion;
  @Autowired private EntityManager em;
  @Autowired private JdbcTemplate jdbc;

  private void vaciarMemoria() {
    em.flush();
    em.clear(); // obliga a que la lectura venga de la base de datos
  }

  private ProgramaEntity nuevoPrograma() {
    return programas.save(
        new ProgramaEntity(UUID.randomUUID(), "Ingenieria de Sistemas", "2879", "Ingenieria"));
  }

  private NormaEntity nuevaNorma() {
    return normas.save(
        new NormaEntity(
            UUID.randomUUID(),
            "ACUERDO",
            "003-A",
            2022,
            "Consejo Superior",
            "BASE",
            LocalDate.of(2022, 1, 1),
            new ColumnasDeMomento("FECHA", LocalDate.of(2022, 2, 1), null),
            null,
            null,
            "PROGRAMAS",
            "{}"));
  }

  private ReglaEntity nuevaRegla(NormaEntity norma) {
    return reglas.save(
        new ReglaEntity(
            UUID.randomUUID(),
            norma.getId(),
            "OTRA",
            "Objeto",
            "Enunciado",
            "Articulo 1",
            null,
            null,
            "DEL_PLAN",
            null,
            null,
            null,
            null,
            null,
            "{\"tipo\":\"OTRA\"}"));
  }

  private VersionEntity nuevaVersion(ConsolidadoEntity consolidado, int numero) {
    return new VersionEntity(
        UUID.randomUUID(),
        consolidado.getId(),
        consolidado.getProgramaId(),
        numero,
        LocalDate.of(2026, 10, 22),
        "INCORPORACION",
        null,
        null,
        1,
        "[]");
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
  void tt04_guardaYLeeUnConsolidadoConSuVersionYSusReglas() {
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
                null,
                null,
                null));
    ConsolidadoEntity consolidado =
        consolidados.save(new ConsolidadoEntity(UUID.randomUUID(), programa.getId()));
    VersionEntity version = versiones.save(nuevaVersion(consolidado, 1));
    ReglaEnVersionEntity enVersion =
        reglasEnVersion.save(
            new ReglaEnVersionEntity(UUID.randomUUID(), version.getId(), regla.getId(), "VIGENTE"));
    vaciarMemoria();

    assertThat(reglasEnVersion.findById(enVersion.getId()))
        .get()
        .extracting(ReglaEnVersionEntity::getEstado)
        .isEqualTo("VIGENTE");
    assertThat(consolidados.findById(consolidado.getId())).isPresent();
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
        new ProgramaEntity(UUID.randomUUID(), "Ingenieria Civil", "2501", "Ingenieria");

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
    ConsolidadoEntity consolidado =
        consolidados.save(new ConsolidadoEntity(UUID.randomUUID(), programa.getId()));
    VersionEntity original = nuevaVersion(consolidado, 1);
    versiones.saveAndFlush(original);
    vaciarMemoria();

    VersionEntity repetida =
        new VersionEntity(
            original.getId(),
            original.getConsolidadoId(),
            original.getProgramaId(),
            7,
            original.getFecha(),
            "OTRO",
            null,
            null,
            1,
            "[]");

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
            "BASE",
            LocalDate.of(2023, 1, 1),
            new ColumnasDeMomento("FECHA", LocalDate.of(2023, 2, 1), null),
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
  void tt04_unMomentoIncoherenteViolaLaRestriccion() {
    NormaEntity norma = nuevaNorma();
    final ReglaEntity regla = nuevaRegla(norma);
    vaciarMemoria();

    assertThatThrownBy(
            () ->
                jdbc.update(
                    "UPDATE norma SET aplica_desde_tipo = 'PERIODO', aplica_desde_fecha = DATE"
                        + " '2020-01-01', aplica_desde_periodo = NULL WHERE id = ?",
                    norma.getId()))
        .isInstanceOf(DataIntegrityViolationException.class);
    assertThatThrownBy(
            () ->
                jdbc.update(
                    "UPDATE norma SET aplica_desde_tipo = 'FECHA', aplica_desde_fecha = NULL"
                        + " WHERE id = ?",
                    norma.getId()))
        .isInstanceOf(DataIntegrityViolationException.class);
    assertThatThrownBy(
            () ->
                jdbc.update(
                    "UPDATE norma SET aplica_desde_tipo = 'OTRO', aplica_desde_fecha = NULL,"
                        + " aplica_desde_periodo = NULL WHERE id = ?",
                    norma.getId()))
        .isInstanceOf(DataIntegrityViolationException.class);
    assertThatThrownBy(
            () ->
                jdbc.update(
                    "UPDATE regla SET vigente_desde_tipo = 'PERIODO', vigente_desde_fecha = DATE"
                        + " '2020-01-01' WHERE id = ?",
                    regla.getId()))
        .isInstanceOf(DataIntegrityViolationException.class);
    assertThatThrownBy(
            () ->
                jdbc.update(
                    "UPDATE norma SET vigente_hasta_tipo = NULL, vigente_hasta_fecha = DATE"
                        + " '2030-01-01' WHERE id = ?",
                    norma.getId()))
        .isInstanceOf(DataIntegrityViolationException.class);
    assertThatThrownBy(
            () ->
                jdbc.update(
                    "UPDATE regla SET afectacion_desde_tipo = NULL, afectacion_desde_fecha = DATE"
                        + " '2030-01-01' WHERE id = ?",
                    regla.getId()))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void tt04_unaReglaConFinSinInicioViolaLaRestriccion() {
    NormaEntity norma = nuevaNorma();
    final ReglaEntity regla = nuevaRegla(norma);
    vaciarMemoria();

    assertThatThrownBy(
            () ->
                jdbc.update(
                    "UPDATE regla SET vigente_hasta_tipo = 'FECHA', vigente_hasta_fecha = DATE"
                        + " '2030-01-01' WHERE id = ?",
                    regla.getId()))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void tt04_unaAfectacionConDestinoIncoherenteViolaLaRestriccion() {
    NormaEntity norma = nuevaNorma();
    final ReglaEntity regla = nuevaRegla(norma);
    vaciarMemoria();

    assertThatThrownBy(
            () ->
                jdbc.update(
                    "UPDATE regla SET tipo_afectacion = 'MODIFICA', regla_afectada_id = ?,"
                        + " norma_afectada_id = ? WHERE id = ?",
                    UUID.randomUUID(),
                    norma.getId(),
                    regla.getId()))
        .isInstanceOf(DataIntegrityViolationException.class);
    assertThatThrownBy(
            () ->
                jdbc.update(
                    "UPDATE regla SET tipo_afectacion = 'MODIFICA', articulo_afectado = 'art. 7'"
                        + " WHERE id = ?",
                    regla.getId()))
        .isInstanceOf(DataIntegrityViolationException.class);
    assertThatThrownBy(
            () ->
                jdbc.update(
                    "UPDATE regla SET tipo_afectacion = 'MODIFICA' WHERE id = ?", regla.getId()))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void tt04_unArticuloAfectadoDeUnaNormaInexistenteViolaLaLlaveForanea() {
    NormaEntity norma = nuevaNorma();
    final ReglaEntity regla = nuevaRegla(norma);
    vaciarMemoria();

    assertThatThrownBy(
            () ->
                jdbc.update(
                    "UPDATE regla SET tipo_afectacion = 'MODIFICA', articulo_afectado = 'art. 7',"
                        + " norma_afectada_id = ? WHERE id = ?",
                    UUID.randomUUID(),
                    regla.getId()))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void tt04_dosVersionesDelMismoProgramaConElMismoNumeroFallan() {
    ProgramaEntity programa = nuevoPrograma();
    ConsolidadoEntity consolidado =
        consolidados.save(new ConsolidadoEntity(UUID.randomUUID(), programa.getId()));
    versiones.saveAndFlush(nuevaVersion(consolidado, 1));

    assertThatThrownBy(() -> versiones.saveAndFlush(nuevaVersion(consolidado, 1)))
        .isInstanceOf(DataIntegrityViolationException.class);
  }
}
