package co.edu.unal.rutaun.normativa.persistencia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;

import co.edu.unal.rutaun.soporte.PruebaDeIntegracion;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
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
  @Autowired private VersionDelConsolidadoRepository versiones;
  @Autowired private EntityManager em;

  private void vaciarMemoria() {
    em.flush();
    em.clear(); // obliga a que la lectura venga de la base de datos
  }

  private ProgramaEntity nuevoPrograma() {
    return programas.save(
        new ProgramaEntity(UUID.randomUUID(), "Ingenieria de Sistemas", "2879", "Ingenieria"));
  }

  private NormaEntity nuevaNorma() {
    return nuevaNorma(List.of());
  }

  private NormaEntity nuevaNorma(List<ReglaEntity> reglas) {
    return new NormaEntity(
        UUID.randomUUID(),
        "ACUERDO",
        "T8-" + UUID.randomUUID().toString().substring(0, 8),
        2022,
        "Consejo Superior",
        "BASE",
        LocalDate.of(2022, 1, 1),
        LocalDate.of(2022, 2, 1),
        null,
        null,
        "PROGRAMAS",
        "{}",
        reglas);
  }

  private static ReglaEntity nuevaRegla(String objeto) {
    return new ReglaEntity(
        UUID.randomUUID(),
        "OTRA",
        objeto,
        "Regla informativa.",
        "Articulo 1",
        null,
        null,
        "INFORMATIVA",
        null,
        null,
        null,
        "{}");
  }

  private VersionEntity nuevaVersion(UUID programaId, int numero) {
    return new VersionEntity(
        UUID.randomUUID(),
        programaId,
        numero,
        FECHA,
        "INCORPORACION",
        null,
        null,
        1,
        "[]",
        null,
        List.of());
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
                    new ProgramaEntity(UUID.randomUUID(), "Otro nombre", "2879", "Ingenieria")))
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
    NormaEntity norma = normas.save(nuevaNorma());
    vaciarMemoria();

    assertThat(em.find(NormaEntity.class, norma.getId()))
        .isNotNull()
        .extracting(NormaEntity::getNumero)
        .isEqualTo(norma.getNumero());
  }

  @Test
  void tt04_unaNormaSeGuardaConSusReglasYSeLeeIgual() {
    ReglaEntity primera = nuevaRegla("Primera regla");
    ReglaEntity segunda = nuevaRegla("Segunda regla");
    NormaEntity norma = normas.save(nuevaNorma(List.of(primera, segunda)));
    vaciarMemoria();

    List<ReglaEntity> leidas = em.find(NormaEntity.class, norma.getId()).getReglas();

    assertThat(leidas).hasSize(2);
    assertThat(leidas)
        .extracting(ReglaEntity::getObjeto)
        .containsExactlyInAnyOrder("Primera regla", "Segunda regla");
    assertThat(leidas).allSatisfy(regla -> assertThat(regla.getDatos()).isEqualTo("{}"));
  }

  @Test
  void tt04_leerUnaNormaPorUnaReglaTraeTodasSusReglas() {
    ReglaEntity primera = nuevaRegla("Primera regla");
    ReglaEntity segunda = nuevaRegla("Segunda regla");
    normas.save(nuevaNorma(List.of(primera, segunda)));
    vaciarMemoria();

    List<NormaEntity> encontradas = normas.findDeLasReglas(List.of(primera.getId()));

    assertThat(encontradas).hasSize(1);
    assertThat(encontradas.get(0).getReglas()).hasSize(2);
  }

  @Test
  void tt04_guardarUnaNormaNoActualizaLaLlaveDeSusReglas() {
    Statistics estadisticas =
        em.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();
    estadisticas.setStatisticsEnabled(true);
    estadisticas.clear();

    normas.save(nuevaNorma(List.of(nuevaRegla("Primera regla"), nuevaRegla("Segunda regla"))));
    em.flush();

    assertThat(estadisticas.getPrepareStatementCount()).isEqualTo(3);
  }

  @Test
  void uc07_ext6b_laGuardaEncuentraUnaReglaPorSuNorma() {
    ReglaEntity regla = nuevaRegla("Regla registrada");
    normas.save(nuevaNorma(List.of(regla)));
    em.flush();

    assertThat(normas.existsByReglasId(regla.getId())).isTrue();
    assertThat(normas.existsByReglasId(UUID.randomUUID())).isFalse();
  }

  @Test
  void tt04_lasNormasQueAfectanAUnaReglaSeEncuentran() {
    ReglaEntity afectada = nuevaRegla("Regla afectada");
    normas.save(nuevaNorma(List.of(afectada)));
    em.flush();

    ReglaEntity afectante =
        new ReglaEntity(
            UUID.randomUUID(),
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
            "{}");
    NormaEntity normaAfectante = nuevaNorma(List.of(afectante));
    normas.save(normaAfectante);
    em.flush();

    List<NormaEntity> encontradas = normas.findDeLasReglasQueAfectanA(afectada.getId());

    assertThat(encontradas).extracting(NormaEntity::getId).containsExactly(normaAfectante.getId());
  }

  @Test
  void tt04_guardaYLeeUnaReglaConSusDatosJson() {
    String json = "{\"materia\":\"2015714\",\"creditos\":3}";
    ReglaEntity regla =
        new ReglaEntity(
            UUID.randomUUID(),
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
            json);
    normas.save(nuevaNorma(List.of(regla)));
    vaciarMemoria();

    assertThat(
            normas.findDeLasReglas(List.of(regla.getId())).stream()
                .flatMap(norma -> norma.getReglas().stream())
                .filter(guardada -> guardada.getId().equals(regla.getId()))
                .findFirst())
        .get()
        .extracting(ReglaEntity::getDatos)
        .isEqualTo(json);
  }

  @Test
  void uc07_ext6b_unaAfectacionAUnaReglaInexistenteFalla() {
    ReglaEntity afectante =
        new ReglaEntity(
            UUID.randomUUID(),
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
            "{}");

    assertThatThrownBy(
            () -> {
              normas.save(nuevaNorma(List.of(afectante)));
              em.flush();
            })
        .isInstanceOf(PersistenceException.class)
        .hasMessageContaining("FK_REGLA_AFECTADA");
  }

  @Test
  void tt04_unaAfectacionAUnaReglaGuardadaSeAcepta() {
    ReglaEntity afectada = nuevaRegla("Regla afectada");
    normas.save(nuevaNorma(List.of(afectada)));
    em.flush();
    ReglaEntity afectante =
        new ReglaEntity(
            UUID.randomUUID(),
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
            "{}");
    NormaEntity norma = normas.save(nuevaNorma(List.of(afectante)));
    vaciarMemoria();

    assertThat(em.find(NormaEntity.class, norma.getId()).getReglas())
        .filteredOn(guardada -> guardada.getId().equals(afectante.getId()))
        .extracting(ReglaEntity::getReglaAfectadaId)
        .containsExactly(afectada.getId());
  }

  @Test
  void tt04_guardaYLeeUnaVersionYSusReglas() {
    ProgramaEntity programa = nuevoPrograma();
    NormaEntity norma = normas.save(nuevaNorma(List.of(nuevaRegla("General"))));
    ReglaEntity regla = norma.getReglas().get(0);
    VersionEntity version =
        versiones.save(
            new VersionEntity(
                UUID.randomUUID(),
                programa.getId(),
                1,
                FECHA,
                "INCORPORACION",
                null,
                null,
                1,
                "[]",
                null,
                List.of(new ReglaEnVersionEntity(UUID.randomUUID(), regla.getId(), "VIGENTE"))));
    vaciarMemoria();

    VersionEntity leida = em.find(VersionEntity.class, version.getId());

    assertThat(leida.getNumero()).isEqualTo(1);
    assertThat(leida.getReglasEnVersion())
        .singleElement()
        .extracting(ReglaEnVersionEntity::getEstado)
        .isEqualTo("VIGENTE");
  }

  @Test
  void tt04_lasReglasEnVersionSeGuardanConSuVersion() {
    ProgramaEntity programa = nuevoPrograma();
    NormaEntity norma =
        normas.save(nuevaNorma(List.of(nuevaRegla("Primera regla"), nuevaRegla("Segunda regla"))));
    List<ReglaEntity> reglas = norma.getReglas();
    VersionEntity version =
        versiones.save(
            new VersionEntity(
                UUID.randomUUID(),
                programa.getId(),
                1,
                FECHA,
                "INCORPORACION",
                null,
                null,
                1,
                "[]",
                null,
                List.of(
                    new ReglaEnVersionEntity(UUID.randomUUID(), reglas.get(0).getId(), "VIGENTE"),
                    new ReglaEnVersionEntity(
                        UUID.randomUUID(), reglas.get(1).getId(), "PROGRAMADA"))));
    vaciarMemoria();

    VersionEntity leida = em.find(VersionEntity.class, version.getId());

    assertThat(leida.getReglasEnVersion()).hasSize(2);
    assertThat(leida.getReglasEnVersion())
        .extracting(ReglaEnVersionEntity::getReglaId)
        .containsExactlyInAnyOrder(reglas.get(0).getId(), reglas.get(1).getId());
    assertThat(leida.getReglasEnVersion())
        .extracting(ReglaEnVersionEntity::getEstado)
        .containsExactlyInAnyOrder("VIGENTE", "PROGRAMADA");
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
    VersionEntity original = nuevaVersion(programa.getId(), 1);
    versiones.save(original);
    em.flush();
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
            null,
            List.of());

    Throwable error =
        catchThrowable(
            () -> {
              versiones.save(repetida);
              em.flush();
            });

    assertThat(error).isInstanceOf(PersistenceException.class);
    em.clear();
    VersionEntity quedo = em.find(VersionEntity.class, original.getId());
    LOG.info(
        "Guardar una versión nueva con un id repetido falló con {}; la fila quedó con número {} y"
            + " motivo {}",
        error.getClass().getSimpleName(),
        quedo.getNumero(),
        quedo.getMotivo());
    assertThat(quedo.getNumero()).isEqualTo(1);
    assertThat(quedo.getMotivo()).isEqualTo("INCORPORACION");
    assertThat(em.createQuery("select count(v) from VersionEntity v", Long.class).getSingleResult())
        .isEqualTo(1);
  }

  @Test
  void tt04_guardarComoNuevaUnaNormaConUnIdExistenteFalla() {
    NormaEntity original = normas.save(nuevaNorma());
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
            LocalDate.of(2023, 2, 1),
            null,
            null,
            "PROGRAMAS",
            "{}",
            List.of());

    assertThatThrownBy(
            () -> {
              normas.save(repetida);
              em.flush();
            })
        .isInstanceOf(PersistenceException.class);

    em.clear();
    assertThat(em.find(NormaEntity.class, original.getId()))
        .isNotNull()
        .extracting(NormaEntity::getNumero)
        .isEqualTo(original.getNumero());
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
            diaAutomatico,
            List.of());

    assertThatThrownBy(
            () -> {
              versiones.save(version);
              em.flush();
            })
        .isInstanceOf(PersistenceException.class)
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
    versiones.save(nuevaVersion(programa.getId(), 1));
    em.flush();

    assertThatThrownBy(
            () -> {
              versiones.save(nuevaVersion(programa.getId(), 1));
              em.flush();
            })
        .isInstanceOf(PersistenceException.class);
  }
}
