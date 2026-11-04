package co.edu.unal.rutaun.normativa.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.compartido.Creditos;
import co.edu.unal.rutaun.compartido.EstadoRegla;
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
import co.edu.unal.rutaun.normativa.dominio.VersionDelConsolidado;
import co.edu.unal.rutaun.normativa.persistencia.ConsolidadoEntidad;
import co.edu.unal.rutaun.normativa.persistencia.ConsolidadoJpaRepository;
import co.edu.unal.rutaun.normativa.persistencia.NormaJpaRepository;
import co.edu.unal.rutaun.normativa.persistencia.ProgramaJpaRepository;
import co.edu.unal.rutaun.normativa.persistencia.ReglaEnVersionEntidad;
import co.edu.unal.rutaun.normativa.persistencia.ReglaEnVersionJpaRepository;
import co.edu.unal.rutaun.normativa.persistencia.ReglaEntidad;
import co.edu.unal.rutaun.normativa.persistencia.ReglaJpaRepository;
import co.edu.unal.rutaun.normativa.persistencia.VersionEntidad;
import co.edu.unal.rutaun.normativa.persistencia.VersionJpaRepository;
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

  @Autowired private ProgramaJpaRepository programas;
  @Autowired private ConsolidadoJpaRepository consolidados;
  @Autowired private NormaJpaRepository normas;
  @Autowired private ReglaJpaRepository reglas;
  @Autowired private VersionJpaRepository versiones;
  @Autowired private ReglaEnVersionJpaRepository reglasEnVersion;

  private Programa programa;
  private UUID consolidadoId;
  private Norma norma;
  private final List<UUID> reglasGuardadas = new ArrayList<>();
  private final List<UUID> versionesGuardadas = new ArrayList<>();

  /** Guarda el programa, su consolidado y la norma de la que cuelgan las reglas. */
  @BeforeEach
  void guardarLoQueLaVersionNecesita() {
    programa =
        Programa.registrar(
            "Programa de prueba",
            "T3-" + UUID.randomUUID().toString().substring(0, 8),
            new Facultad("Ingeniería"));
    programas.save(ProgramaMapper.aEntidad(programa));
    consolidadoId = UUID.randomUUID();
    consolidados.save(new ConsolidadoEntidad(consolidadoId, programa.id().valor()));
    norma = ReglasDePrueba.normaNueva();
    normas.save(NormaMapper.aEntidad(norma));
  }

  /** Borra lo que guardó la prueba, en orden inverso, porque aquí sí se hizo commit. */
  @AfterEach
  void limpiar() {
    for (UUID versionId : versionesGuardadas) {
      reglasEnVersion.deleteAll(reglasEnVersion.findByVersionId(versionId));
    }
    versiones.deleteAllById(versionesGuardadas);
    consolidados.deleteById(consolidadoId);
    reglas.deleteAllById(reglasGuardadas);
    normas.deleteById(norma.id().valor());
    programas.deleteById(programa.id().valor());
  }

  @Test
  void tt04_versionIdaYVuelta() {
    List<Regla> deLaVersion =
        List.of(
            guardarRegla(new DatosDeRegla.DePrograma(new Creditos(160)), EstadoRegla.VIGENTE),
            guardarRegla(
                new DatosDeRegla.DeMateria(
                    MallaDePrueba.pequena().materias().get(0),
                    MallaDePrueba.nodo(MallaDePrueba.pequena(), "Fundamentación")),
                EstadoRegla.VIGENTE),
            guardarRegla(
                new DatosDeRegla.DePrerrequisito(
                    new Prerrequisito(
                        new CodigoMateria("9000006"),
                        new ExpresionDeMaterias.MateriaDelPrograma(new CodigoMateria("9000001")))),
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
    Regla regla = guardarRegla(new DatosDeRegla.DeOtraCategoria(), EstadoRegla.VIGENTE);
    List<MotivoDeVersion> motivos =
        List.of(
            new MotivoDeVersion.PorIncorporacion(norma.id(), ADMINISTRADOR),
            new MotivoDeVersion.PorCorreccion(ADMINISTRADOR),
            new MotivoDeVersion.AutomaticoPorPeriodo(
                new PeriodoAcademico(2026, ParteDelAnio.SEGUNDO)),
            new MotivoDeVersion.AutomaticoPorFecha(LocalDate.of(2027, 2, 1)));

    for (int i = 0; i < motivos.size(); i++) {
      VersionDelConsolidado original = version(i + 1, motivos.get(i), List.of(regla));

      VersionDelConsolidado reconstruida = leer(guardar(original));

      assertThat(reconstruida)
          .as("versión con el motivo %s", motivos.get(i))
          .usingRecursiveComparison()
          .isEqualTo(original);
    }
  }

  @Test
  void tt04_laMismaReglaTieneElEstadoDeCadaVersion() {
    Regla programada =
        guardarRegla(new DatosDeRegla.DePrograma(new Creditos(160)), EstadoRegla.PROGRAMADA);
    Regla vigente = ReglasDePrueba.conEstado(programada, EstadoRegla.VIGENTE);
    VersionDelConsolidado primera =
        version(
            1,
            new MotivoDeVersion.PorIncorporacion(norma.id(), ADMINISTRADOR),
            List.of(programada));
    VersionDelConsolidado segunda =
        version(2, new MotivoDeVersion.AutomaticoPorFecha(FECHA), List.of(vigente));

    UUID primeraId = guardar(primera);
    UUID segundaId = guardar(segunda);

    // Una sola fila de regla; el estado vive en regla_en_version, una fila por versión.
    assertThat(reglasEnVersion.findByVersionId(primeraId))
        .extracting(ReglaEnVersionEntidad::getEstado)
        .containsExactly("PROGRAMADA");
    assertThat(reglasEnVersion.findByVersionId(segundaId))
        .extracting(ReglaEnVersionEntidad::getEstado)
        .containsExactly("VIGENTE");
    assertThat(leer(primeraId).reglas())
        .extracting(Regla::estado)
        .containsExactly(EstadoRegla.PROGRAMADA);
    assertThat(leer(segundaId).reglas())
        .extracting(Regla::estado)
        .containsExactly(EstadoRegla.VIGENTE);
    assertThat(leer(primeraId)).usingRecursiveComparison().isEqualTo(primera);
    assertThat(leer(segundaId)).usingRecursiveComparison().isEqualTo(segunda);
  }

  @Test
  void tt04_versionConMotivoDesconocidoNoSeReconstituye() {
    VersionEntidad invalida = versionAMano(UUID.randomUUID(), "POR_DECRETO", "{}");

    assertThatThrownBy(() -> VersionDelConsolidadoMapper.aDominio(invalida, List.of(), Map.of()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("POR_DECRETO");
  }

  @Test
  void tt04_versionConUnaReglaQueFaltaNoSeReconstituye() {
    VersionEntidad version =
        versionAMano(UUID.randomUUID(), "POR_CORRECCION", "{\"administrador\":\"a\"}");
    UUID reglaQueFalta = UUID.randomUUID();
    ReglaEnVersionEntidad fila =
        new ReglaEnVersionEntidad(UUID.randomUUID(), version.getId(), reglaQueFalta, "VIGENTE");

    assertThatThrownBy(() -> VersionDelConsolidadoMapper.aDominio(version, List.of(fila), Map.of()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining(reglaQueFalta.toString());
  }

  @Test
  void tt04_versionConUnaFilaDeOtraVersionNoSeReconstituye() {
    VersionEntidad version =
        versionAMano(UUID.randomUUID(), "POR_CORRECCION", "{\"administrador\":\"a\"}");
    ReglaEnVersionEntidad deOtra =
        new ReglaEnVersionEntidad(
            UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "VIGENTE");

    assertThatThrownBy(
            () -> VersionDelConsolidadoMapper.aDominio(version, List.of(deOtra), Map.of()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("no es de la versión");
  }

  @Test
  void tt04_versionConUnMotivoQueNoCabeEnLaColumnaNoSeGuarda() {
    VersionDelConsolidado larga =
        version(1, new MotivoDeVersion.PorCorreccion("a".repeat(300)), List.of());

    assertThatThrownBy(() -> VersionDelConsolidadoMapper.aEntidades(larga, consolidadoId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("detalle_motivo");
  }

  @Test
  void tt04_guardarDosVecesLaMismaVersionFalla() {
    Regla regla = guardarRegla(new DatosDeRegla.DeOtraCategoria(), EstadoRegla.VIGENTE);
    VersionDelConsolidado original =
        version(1, new MotivoDeVersion.PorIncorporacion(norma.id(), ADMINISTRADOR), List.of(regla));
    guardar(original);
    VersionDelConsolidadoMapper.Filas otraVez =
        VersionDelConsolidadoMapper.aEntidades(original, consolidadoId);

    // Una versión es de solo agregar: la restricción de (programa, número) rechaza la segunda.
    assertThatThrownBy(() -> versiones.saveAndFlush(otraVez.version()))
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessageContaining("UQ_VERSION_PROGRAMA_NUMERO");
  }

  /** Guarda una regla de la norma de la prueba y devuelve la regla de dominio. */
  private Regla guardarRegla(DatosDeRegla datos, EstadoRegla estado) {
    Regla regla = ReglasDePrueba.regla(norma, datos, estado);
    reglas.save(ReglaMapper.aEntidad(regla));
    reglasGuardadas.add(regla.id().valor());
    return regla;
  }

  private VersionDelConsolidado version(
      int numero, MotivoDeVersion motivo, List<Regla> deLaVersion) {
    return VersionDelConsolidado.reconstituir(programa.id(), numero, FECHA, motivo, deLaVersion, 1);
  }

  /** Guarda las filas de la versión y devuelve el identificador de su fila. */
  private UUID guardar(VersionDelConsolidado version) {
    VersionDelConsolidadoMapper.Filas filas =
        VersionDelConsolidadoMapper.aEntidades(version, consolidadoId);
    versiones.save(filas.version());
    versionesGuardadas.add(filas.version().getId());
    reglasEnVersion.saveAll(filas.reglasEnVersion());
    return filas.version().getId();
  }

  /** Lee las filas de la base, como lo haría quien consulta una versión, y la reconstruye. */
  private VersionDelConsolidado leer(UUID versionId) {
    VersionEntidad fila = versiones.findById(versionId).orElseThrow();
    List<ReglaEnVersionEntidad> delasReglas = reglasEnVersion.findByVersionId(versionId);
    Map<UUID, ReglaEntidad> porId = new HashMap<>();
    reglas
        .findAllById(delasReglas.stream().map(ReglaEnVersionEntidad::getReglaId).toList())
        .forEach(regla -> porId.put(regla.getId(), regla));
    return VersionDelConsolidadoMapper.aDominio(fila, delasReglas, porId);
  }

  private VersionEntidad versionAMano(UUID id, String motivo, String detalle) {
    return new VersionEntidad(
        id, consolidadoId, programa.id().valor(), 1, FECHA, motivo, detalle, null, 1, null);
  }
}
