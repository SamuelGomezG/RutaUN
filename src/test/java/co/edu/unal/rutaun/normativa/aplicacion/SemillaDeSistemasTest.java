package co.edu.unal.rutaun.normativa.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.unal.rutaun.normativa.persistencia.ConsolidadoJpaRepository;
import co.edu.unal.rutaun.normativa.persistencia.NormaJpaRepository;
import co.edu.unal.rutaun.normativa.persistencia.ProgramaJpaRepository;
import co.edu.unal.rutaun.normativa.persistencia.ReglaEnVersionEntidad;
import co.edu.unal.rutaun.normativa.persistencia.ReglaEnVersionJpaRepository;
import co.edu.unal.rutaun.normativa.persistencia.ReglaEntidad;
import co.edu.unal.rutaun.normativa.persistencia.ReglaJpaRepository;
import co.edu.unal.rutaun.normativa.persistencia.VersionEntidad;
import co.edu.unal.rutaun.normativa.persistencia.VersionJpaRepository;
import co.edu.unal.rutaun.soporte.PruebaDeIntegracion;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.transaction.annotation.Transactional;

/**
 * Verifica que la semilla real de Sistemas se carga completa con el runner de desarrollo.
 *
 * <p>TT-04-T5 (#131, #100): corre {@link SemillaDeDesarrolloRunner} sobre H2 en memoria y comprueba
 * programas, normas, reglas, consolidados, versiones y reglas por versión, una y dos veces.
 *
 * <p>Las cifras son las de la semilla del 2026-11-14. Cambian con #133 (casos que faltan), #178
 * (agrupaciones repetidas) y #146 (orden de carga): quien las cambie actualiza las constantes de
 * esta clase, el número de llamadas de {@code SemillaDeDesarrolloRunnerTest} y la prueba de #130 en
 * {@code ImportacionDeNormasServiceTest}.
 *
 * <p>Con {@code @Transactional}, las dos corridas comparten transacción: la prueba demuestra que la
 * segunda omite lo ya cargado dentro de ella. Que tampoco duplica entre dos arranques con la base
 * en archivo se demuestra a mano en el paso 3 de «Cómo ejecutarlo» del PR.
 *
 * <p>{@code src/test/resources/semilla/} no debe existir: cualquier archivo ahí oculta la semilla
 * real en las pruebas, porque {@code classpath:semilla/*.json} resuelve solo la primera carpeta
 * {@code semilla/} del classpath.
 */
@PruebaDeIntegracion
@Transactional
class SemillaDeSistemasTest {

  private static final Logger LOG = LoggerFactory.getLogger(SemillaDeSistemasTest.class);

  private static final int PROGRAMAS = 1;
  private static final int NORMAS = 3;
  private static final int REGLAS = 276;
  private static final int CONSOLIDADOS = 1;
  private static final List<Integer> REGLAS_POR_VERSION = List.of(20, 27, 276);
  private static final int VERSIONES = REGLAS_POR_VERSION.size();
  private static final int REGLAS_EN_VERSION =
      REGLAS_POR_VERSION.stream().mapToInt(Integer::intValue).sum();
  private static final int REGLAS_DE_LA_VIGENTE =
      REGLAS_POR_VERSION.get(REGLAS_POR_VERSION.size() - 1);
  private static final Cifras ESPERADAS =
      new Cifras(
          PROGRAMAS,
          NORMAS,
          REGLAS,
          CONSOLIDADOS,
          VERSIONES,
          REGLAS_EN_VERSION,
          REGLAS_POR_VERSION);

  private final ImportacionDeProgramasService programas;
  private final ImportacionDeNormasService normas;
  private final ProgramaJpaRepository programasGuardados;
  private final NormaJpaRepository normasGuardadas;
  private final ReglaJpaRepository reglas;
  private final ConsolidadoJpaRepository consolidados;
  private final VersionJpaRepository versiones;
  private final ReglaEnVersionJpaRepository reglasEnVersion;

  /** Recibe los servicios y repositorios que Spring inyecta. */
  @Autowired
  SemillaDeSistemasTest(
      ImportacionDeProgramasService programas,
      ImportacionDeNormasService normas,
      ProgramaJpaRepository programasGuardados,
      NormaJpaRepository normasGuardadas,
      ReglaJpaRepository reglas,
      ConsolidadoJpaRepository consolidados,
      VersionJpaRepository versiones,
      ReglaEnVersionJpaRepository reglasEnVersion) {
    this.programas = programas;
    this.normas = normas;
    this.programasGuardados = programasGuardados;
    this.normasGuardadas = normasGuardadas;
    this.reglas = reglas;
    this.consolidados = consolidados;
    this.versiones = versiones;
    this.reglasEnVersion = reglasEnVersion;
  }

  @BeforeEach
  void comprobarOrigenDeLaSemilla() throws IOException {
    URL produccion =
        SemillaDeDesarrolloRunner.class.getProtectionDomain().getCodeSource().getLocation();
    List<URL> raices =
        Collections.list(Thread.currentThread().getContextClassLoader().getResources("semilla/"));

    assertThat(raices)
        .as(
            "semilla/ debe resolverse solo desde la salida de producción; "
                + "borra src/test/resources/semilla/ y corre ./mvnw clean")
        .isNotEmpty()
        .allSatisfy(raiz -> assertThat(raiz.getPath()).startsWith(produccion.getPath()));
  }

  @Test
  void tt04_laSemillaCompletaDejaTresNormasYUnaVersionVigenteConTodasSusReglas() {
    cargarLaSemilla();

    Cifras cifras = cifras();
    registrar(cifras);
    assertThat(cifras)
        .as("la semilla cambió (actualiza las constantes) o una prueba dejó filas sin revertir")
        .isEqualTo(ESPERADAS);

    VersionEntidad vigente = versionVigente();
    assertThat(vigente.getNumero()).isEqualTo(VERSIONES);
    assertThat(reglasEnVersion.findByVersionId(vigente.getId())).hasSize(REGLAS_DE_LA_VIGENTE);
    assertThat(normasDeLasReglasDe(vigente)).hasSize(NORMAS);
  }

  @Test
  void tt04_cargarDosVecesLaSemillaNoDuplica() {
    cargarLaSemilla();
    Cifras primera = cifras();
    final Set<UUID> antes = identificadores();

    cargarLaSemilla();

    Cifras segunda = cifras();
    registrar(segunda);
    assertThat(segunda)
        .as("la segunda corrida debe dejar las mismas cifras")
        .isEqualTo(primera)
        .isEqualTo(ESPERADAS);
    assertThat(identificadores())
        .as("la segunda corrida no debe borrar y recrear filas")
        .isEqualTo(antes);
  }

  private void cargarLaSemilla() {
    new SemillaDeDesarrolloRunner(programas, normas).run(new DefaultApplicationArguments());
  }

  private Cifras cifras() {
    UUID programaId = programaDeSistemas();
    List<VersionEntidad> ordenadas =
        versiones.findAll().stream()
            .filter(version -> programaId.equals(version.getProgramaId()))
            .sorted(Comparator.comparingInt(VersionEntidad::getNumero))
            .toList();
    return new Cifras(
        programasGuardados.count(),
        normasGuardadas.count(),
        reglas.count(),
        consolidados.count(),
        ordenadas.size(),
        reglasEnVersion.count(),
        ordenadas.stream()
            .map(version -> reglasEnVersion.findByVersionId(version.getId()).size())
            .toList());
  }

  private UUID programaDeSistemas() {
    return programasGuardados.findByCodigoSia("2879").orElseThrow().getId();
  }

  private VersionEntidad versionVigente() {
    return versiones.findTopByProgramaIdOrderByNumeroDesc(programaDeSistemas()).orElseThrow();
  }

  private Set<UUID> identificadores() {
    Set<UUID> ids = new HashSet<>();
    programasGuardados.findAll().forEach(programa -> ids.add(programa.getId()));
    normasGuardadas.findAll().forEach(norma -> ids.add(norma.getId()));
    reglas.findAll().forEach(regla -> ids.add(regla.getId()));
    versiones.findAll().forEach(version -> ids.add(version.getId()));
    return ids;
  }

  private Set<UUID> normasDeLasReglasDe(VersionEntidad version) {
    Set<UUID> reglaIds =
        reglasEnVersion.findByVersionId(version.getId()).stream()
            .map(ReglaEnVersionEntidad::getReglaId)
            .collect(Collectors.toSet());
    return reglas.findAllById(reglaIds).stream()
        .map(ReglaEntidad::getNormaId)
        .collect(Collectors.toSet());
  }

  private static void registrar(Cifras cifras) {
    LOG.info(
        "Semilla cargada: {} programa, {} normas, {} reglas, {} versiones; vigente: {}",
        cifras.programas(),
        cifras.normas(),
        cifras.reglas(),
        cifras.versiones(),
        cifras.reglasPorVersion().get(cifras.reglasPorVersion().size() - 1));
  }

  private record Cifras(
      long programas,
      long normas,
      long reglas,
      long consolidados,
      long versiones,
      long reglasEnVersion,
      List<Integer> reglasPorVersion) {}
}
