package co.edu.unal.rutaun.normativa.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.compartido.Creditos;
import co.edu.unal.rutaun.compartido.EstadoRegla;
import co.edu.unal.rutaun.compartido.NormaId;
import co.edu.unal.rutaun.compartido.ParteDelAnio;
import co.edu.unal.rutaun.compartido.PeriodoAcademico;
import co.edu.unal.rutaun.normativa.dominio.DatosDeRegla;
import co.edu.unal.rutaun.normativa.dominio.ExpresionDeMaterias;
import co.edu.unal.rutaun.normativa.dominio.Facultad;
import co.edu.unal.rutaun.normativa.dominio.MotivoDeVersion;
import co.edu.unal.rutaun.normativa.dominio.Norma;
import co.edu.unal.rutaun.normativa.dominio.Prerrequisito;
import co.edu.unal.rutaun.normativa.dominio.Programa;
import co.edu.unal.rutaun.normativa.dominio.Regla;
import co.edu.unal.rutaun.normativa.dominio.ReglaEnVersion;
import co.edu.unal.rutaun.normativa.dominio.VersionDelConsolidado;
import co.edu.unal.rutaun.normativa.persistencia.NormaEntity;
import co.edu.unal.rutaun.normativa.persistencia.NormaRepository;
import co.edu.unal.rutaun.normativa.persistencia.ProgramaRepository;
import co.edu.unal.rutaun.normativa.persistencia.ReglaEnVersionEntity;
import co.edu.unal.rutaun.normativa.persistencia.ReglaEntity;
import co.edu.unal.rutaun.normativa.persistencia.VersionDelConsolidadoRepository;
import co.edu.unal.rutaun.normativa.persistencia.VersionEntity;
import co.edu.unal.rutaun.soporte.MallaDePrueba;
import co.edu.unal.rutaun.soporte.PruebaDeIntegracion;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * TT-04-T3 (#91): ida y vuelta de {@link VersionDelConsolidado} (dominio, entidades, base,
 * entidades, dominio).
 *
 * <p>No lleva {@code @Transactional}: cada operación del repositorio hace commit de verdad, así la
 * lectura sale de la base y no de la memoria de Hibernate. Las reglas de cada versión van todas
 * vigentes, salvo en la prueba que verifica que el estado vive en la versión.
 */
@PruebaDeIntegracion
class VersionDelConsolidadoMapperTest {

  private static final LocalDate FECHA = LocalDate.of(2026, 11, 3);
  private static final String ADMINISTRADOR = "semilla";

  @Autowired private ProgramaRepository programas;
  @Autowired private NormaRepository normas;
  @Autowired private VersionDelConsolidadoRepository versiones;
  @Autowired private JdbcTemplate jdbc;
  @Autowired private TransactionTemplate transaccion;

  private Programa programa;
  private Norma norma;
  private final List<UUID> programasGuardados = new ArrayList<>();
  private final List<UUID> normasGuardadas = new ArrayList<>();
  private final List<UUID> reglasGuardadas = new ArrayList<>();
  private final List<UUID> versionesGuardadas = new ArrayList<>();

  /** Guarda el programa y la norma de la que cuelgan las reglas. */
  @BeforeEach
  void guardarLoQueLaVersionNecesita() {
    programa =
        Programa.registrar(
            "Programa de prueba",
            "T3-" + UUID.randomUUID().toString().substring(0, 8),
            new Facultad("Ingeniería"));
    programas.save(ProgramaMapper.aEntidad(programa));
    programasGuardados.add(programa.id().valor());
    norma = ReglasDePrueba.normaNueva();
    normas.save(NormaMapper.aEntidad(norma, List.of()));
    normasGuardadas.add(norma.id().valor());
  }

  /** Borra lo que guardó la prueba, en orden inverso, porque aquí sí se hizo commit. */
  @AfterEach
  void limpiar() {
    for (UUID versionId : versionesGuardadas) {
      jdbc.update(
          "DELETE FROM regla_en_version WHERE CAST(version_id AS VARCHAR) = ?",
          versionId.toString());
    }
    versiones.deleteAllById(versionesGuardadas);
    for (UUID reglaId : reglasGuardadas) {
      jdbc.update("DELETE FROM regla WHERE CAST(id AS VARCHAR) = ?", reglaId.toString());
    }
    for (UUID normaId : normasGuardadas) {
      jdbc.update("DELETE FROM norma WHERE CAST(id AS VARCHAR) = ?", normaId.toString());
    }
    programasGuardados.forEach(programas::deleteById);
  }

  @Test
  void tt04_versionIdaYVuelta() {
    List<ReglaEnVersion> deLaVersion =
        List.of(
            new ReglaEnVersion(
                guardarRegla(new DatosDeRegla.DePrograma(new Creditos(160))), EstadoRegla.VIGENTE),
            new ReglaEnVersion(
                guardarRegla(
                    new DatosDeRegla.DeMateria(
                        MallaDePrueba.pequena().materias().get(0),
                        MallaDePrueba.nodo(MallaDePrueba.pequena(), "Fundamentación"))),
                EstadoRegla.VIGENTE),
            new ReglaEnVersion(
                guardarRegla(
                    new DatosDeRegla.DePrerrequisito(
                        new Prerrequisito(
                            new CodigoMateria("9000006"),
                            new ExpresionDeMaterias.MateriaDelPrograma(
                                new CodigoMateria("9000001"))))),
                EstadoRegla.VIGENTE));
    VersionDelConsolidado original =
        version(1, new MotivoDeVersion.PorIncorporacion(norma.id(), ADMINISTRADOR), deLaVersion);

    UUID versionId = guardar(original);
    VersionDelConsolidado reconstruida = leer(versionId);

    // La versión y la regla solo comparan su identidad; la comparación recursiva revisa todo. Las
    // reglas se leen ordenadas por id, así que no se compara el orden.
    assertThat(reconstruida)
        .usingRecursiveComparison()
        .ignoringCollectionOrder()
        .isEqualTo(original);
    assertThat(reconstruida.reglas()).hasSize(3);
  }

  @Test
  void tt04_versionConCadaMotivoIdaYVuelta() {
    Regla regla = guardarRegla(new DatosDeRegla.DeOtraCategoria());
    ReglaEnVersion enVersion = new ReglaEnVersion(regla, EstadoRegla.VIGENTE);
    List<MotivoDeVersion> motivos =
        List.of(
            new MotivoDeVersion.PorIncorporacion(norma.id(), ADMINISTRADOR),
            new MotivoDeVersion.PorCorreccion(ADMINISTRADOR),
            new MotivoDeVersion.AutomaticoPorPeriodo(
                new PeriodoAcademico(2026, ParteDelAnio.SEGUNDO)),
            new MotivoDeVersion.AutomaticoPorFecha(LocalDate.of(2027, 2, 1)));

    for (int i = 0; i < motivos.size(); i++) {
      VersionDelConsolidado original = version(i + 1, motivos.get(i), List.of(enVersion));

      VersionDelConsolidado reconstruida = leer(guardar(original));

      assertThat(reconstruida)
          .as("versión con el motivo %s", motivos.get(i))
          .usingRecursiveComparison()
          .isEqualTo(original);
    }
  }

  @Test
  void tt04_laMismaReglaTieneElEstadoDeCadaVersion() {
    Regla regla = guardarRegla(new DatosDeRegla.DePrograma(new Creditos(160)));
    VersionDelConsolidado primera =
        version(
            1,
            new MotivoDeVersion.PorIncorporacion(norma.id(), ADMINISTRADOR),
            List.of(new ReglaEnVersion(regla, EstadoRegla.PROGRAMADA)));
    VersionDelConsolidado segunda =
        version(
            2,
            new MotivoDeVersion.AutomaticoPorFecha(FECHA),
            List.of(new ReglaEnVersion(regla, EstadoRegla.VIGENTE)));

    UUID primeraId = guardar(primera);
    UUID segundaId = guardar(segunda);

    // Una sola fila de regla; el estado vive en regla_en_version, una fila por versión.
    assertThat(estadosEnVersion(primeraId)).containsExactly("PROGRAMADA");
    assertThat(estadosEnVersion(segundaId)).containsExactly("VIGENTE");
    assertThat(leer(primeraId).reglas())
        .extracting(ReglaEnVersion::estado)
        .containsExactly(EstadoRegla.PROGRAMADA);
    assertThat(leer(segundaId).reglas())
        .extracting(ReglaEnVersion::estado)
        .containsExactly(EstadoRegla.VIGENTE);
    assertThat(leer(primeraId)).usingRecursiveComparison().isEqualTo(primera);
    assertThat(leer(segundaId)).usingRecursiveComparison().isEqualTo(segunda);
  }

  @Test
  void tt04_versionConMotivoDesconocidoNoSeReconstituye() {
    VersionEntity invalida = versionAMano(UUID.randomUUID(), "POR_DECRETO", "{}");

    assertThatThrownBy(() -> VersionDelConsolidadoMapper.aDominio(invalida, Map.of()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("POR_DECRETO");
  }

  @Test
  void tt04_versionConUnaReglaQueFaltaNoSeReconstituye() {
    UUID reglaQueFalta = UUID.randomUUID();
    VersionEntity version =
        versionAMano(
            UUID.randomUUID(),
            "POR_CORRECCION",
            "{\"administrador\":\"a\"}",
            new ReglaEnVersionEntity(UUID.randomUUID(), reglaQueFalta, "VIGENTE"));

    assertThatThrownBy(() -> VersionDelConsolidadoMapper.aDominio(version, Map.of()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining(reglaQueFalta.toString());
  }

  @Test
  void tt04_versionConUnMotivoQueNoCabeEnLaColumnaNoSeGuarda() {
    VersionDelConsolidado larga =
        version(1, new MotivoDeVersion.PorCorreccion("a".repeat(300)), List.of());

    assertThatThrownBy(() -> VersionDelConsolidadoMapper.aEntidad(larga))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("detalle_motivo");
  }

  @Test
  void tt04_versionConUnMotivoDeExactamenteElLargoDeLaColumnaSeGuarda() {
    // Se mide lo que ocupa el JSON sin el administrador, para llenar la columna justo hasta 300.
    int sinAdministrador =
        JsonDeNormativa.mapper().writeValueAsString(new MotivoDeVersion.PorCorreccion("a")).length()
            - 1;
    VersionDelConsolidado justa =
        version(
            1, new MotivoDeVersion.PorCorreccion("a".repeat(300 - sinAdministrador)), List.of());

    UUID versionId = guardar(justa);

    assertThat(versiones.findById(versionId).orElseThrow().getDetalleMotivo()).hasSize(300);
    assertThat(leer(versionId)).usingRecursiveComparison().isEqualTo(justa);
  }

  @Test
  void tt04_guardarDosVecesLaMismaVersionFalla() {
    Regla regla = guardarRegla(new DatosDeRegla.DeOtraCategoria());
    VersionDelConsolidado original =
        version(
            1,
            new MotivoDeVersion.PorIncorporacion(norma.id(), ADMINISTRADOR),
            List.of(new ReglaEnVersion(regla, EstadoRegla.VIGENTE)));
    guardar(original);
    VersionEntity otraVez = VersionDelConsolidadoMapper.aEntidad(original);

    // Una versión es de solo agregar: la restricción de (programa, número) rechaza la segunda.
    assertThatThrownBy(() -> versiones.saveAndFlush(otraVez))
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessageContaining("UQ_VERSION_PROGRAMA_NUMERO");
  }

  @Test
  void s15_dosVersionesAutomaticasDelMismoProgramaElMismoDiaFallan() {
    guardar(version(1, new MotivoDeVersion.AutomaticoPorFecha(FECHA), List.of()));
    VersionDelConsolidado segunda =
        version(2, new MotivoDeVersion.AutomaticoPorFecha(FECHA), List.of());

    assertThatThrownBy(() -> versiones.saveAndFlush(VersionDelConsolidadoMapper.aEntidad(segunda)))
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessageContaining("UQ_VERSION_AUTOMATICA_POR_DIA");
  }

  @Test
  void s15_unaVersionPorFechaYOtrasPorPeriodoElMismoDiaSeAceptan() {
    guardar(version(1, new MotivoDeVersion.AutomaticoPorFecha(FECHA), List.of()));
    UUID segunda =
        guardar(
            version(
                2,
                new MotivoDeVersion.AutomaticoPorPeriodo(
                    new PeriodoAcademico(2026, ParteDelAnio.SEGUNDO)),
                List.of()));
    UUID tercera =
        guardar(
            version(
                3,
                new MotivoDeVersion.AutomaticoPorPeriodo(
                    new PeriodoAcademico(2026, ParteDelAnio.PRIMERO)),
                List.of()));

    assertThat(versiones.findById(segunda)).isPresent();
    assertThat(versiones.findById(tercera)).isPresent();
  }

  @Test
  void tt04_variasVersionesNoAutomaticasElMismoDiaSeAceptan() {
    UUID primera = guardar(version(1, new MotivoDeVersion.PorCorreccion(ADMINISTRADOR), List.of()));
    UUID segunda =
        guardar(version(2, new MotivoDeVersion.PorCorreccion(ADMINISTRADOR + " 2"), List.of()));
    UUID tercera =
        guardar(
            version(3, new MotivoDeVersion.PorIncorporacion(norma.id(), ADMINISTRADOR), List.of()));

    assertThat(versiones.findById(primera)).isPresent();
    assertThat(versiones.findById(segunda)).isPresent();
    assertThat(versiones.findById(tercera)).isPresent();
  }

  @Test
  void s15_elMismoDiaEnProgramasDistintosSeAcepta() {
    Programa otro =
        Programa.registrar(
            "Otro programa",
            "T3-" + UUID.randomUUID().toString().substring(0, 8),
            new Facultad("Ingeniería"));
    programas.save(ProgramaMapper.aEntidad(otro));
    programasGuardados.add(otro.id().valor());
    guardar(version(1, new MotivoDeVersion.AutomaticoPorFecha(FECHA), List.of()));
    VersionDelConsolidado delOtro =
        VersionDelConsolidado.reconstituir(
            otro.id(), 1, FECHA, new MotivoDeVersion.AutomaticoPorFecha(FECHA), List.of(), 1);

    UUID idOtro = guardar(delOtro);

    assertThat(versiones.findById(idOtro)).isPresent();
  }

  @Test
  void tt04_elMapperSoloLlenaElDiaEnLasVersionesPorFecha() {
    List<MotivoDeVersion> motivos =
        List.of(
            new MotivoDeVersion.PorIncorporacion(norma.id(), ADMINISTRADOR),
            new MotivoDeVersion.PorCorreccion(ADMINISTRADOR),
            new MotivoDeVersion.AutomaticoPorPeriodo(
                new PeriodoAcademico(2026, ParteDelAnio.SEGUNDO)),
            new MotivoDeVersion.AutomaticoPorFecha(LocalDate.of(2027, 2, 1)));

    for (int i = 0; i < motivos.size(); i++) {
      MotivoDeVersion motivo = motivos.get(i);
      VersionEntity fila = VersionDelConsolidadoMapper.aEntidad(version(i + 1, motivo, List.of()));
      LocalDate esperado = motivo instanceof MotivoDeVersion.AutomaticoPorFecha ? FECHA : null;

      assertThat(fila.getDiaAutomatico()).as("motivo %s", motivo).isEqualTo(esperado);
    }
  }

  /** Guarda una regla dentro de su propia norma y devuelve la regla de dominio. */
  private Regla guardarRegla(DatosDeRegla datos) {
    Norma normaDeLaRegla = ReglasDePrueba.normaNueva();
    Regla regla = ReglasDePrueba.regla(normaDeLaRegla, datos);
    normas.save(NormaMapper.aEntidad(normaDeLaRegla, List.of(regla)));
    normasGuardadas.add(normaDeLaRegla.id().valor());
    reglasGuardadas.add(regla.id().valor());
    return regla;
  }

  private VersionDelConsolidado version(
      int numero, MotivoDeVersion motivo, List<ReglaEnVersion> deLaVersion) {
    return VersionDelConsolidado.reconstituir(programa.id(), numero, FECHA, motivo, deLaVersion, 1);
  }

  /** Guarda la versión con sus filas y devuelve el identificador de su fila. */
  private UUID guardar(VersionDelConsolidado version) {
    VersionEntity fila = VersionDelConsolidadoMapper.aEntidad(version);
    versiones.save(fila);
    versionesGuardadas.add(fila.getId());
    return fila.getId();
  }

  /** Lee las filas de la base, como lo haría quien consulta una versión, y la reconstruye. */
  private VersionDelConsolidado leer(UUID versionId) {
    return transaccion.execute(
        estado -> {
          VersionEntity fila = versiones.findById(versionId).orElseThrow();
          Map<UUID, Regla> porId = new HashMap<>();
          for (NormaEntity contenedora :
              normas.findDeLasReglas(
                  fila.getReglasEnVersion().stream()
                      .map(ReglaEnVersionEntity::getReglaId)
                      .toList())) {
            NormaId normaId = new NormaId(contenedora.getId());
            for (ReglaEntity regla : contenedora.getReglas()) {
              porId.put(regla.getId(), ReglaMapper.aDominio(regla, normaId));
            }
          }
          return VersionDelConsolidadoMapper.aDominio(fila, porId);
        });
  }

  private List<String> estadosEnVersion(UUID versionId) {
    return jdbc.queryForList(
        "SELECT estado FROM regla_en_version WHERE CAST(version_id AS VARCHAR) = ?",
        String.class,
        versionId.toString());
  }

  private VersionEntity versionAMano(
      UUID id, String motivo, String detalle, ReglaEnVersionEntity... filas) {
    return new VersionEntity(
        id, programa.id().valor(), 1, FECHA, motivo, detalle, null, 1, null, null, List.of(filas));
  }
}
