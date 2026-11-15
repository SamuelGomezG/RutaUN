package co.edu.unal.rutaun.normativa.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.compartido.EstadoRegla;
import co.edu.unal.rutaun.normativa.ClasificacionRegla;
import co.edu.unal.rutaun.normativa.dominio.DatosDeRegla;
import co.edu.unal.rutaun.normativa.dominio.ExpresionDeMaterias;
import co.edu.unal.rutaun.normativa.dominio.Regla;
import co.edu.unal.rutaun.normativa.dominio.TipoDeCargaPorPeriodo;
import co.edu.unal.rutaun.normativa.dominio.Vigencia;
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
import java.util.Map;
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
 * <p>Las cifras son las de la semilla del 2026-11-14, con los dos archivos de datos de prueba de
 * TT-04-T4 (#133). Cambian con #178 (agrupaciones repetidas) y #146 (orden de carga): quien las
 * cambie actualiza estas constantes; si el cambio toca uno de los tres acuerdos, también la prueba
 * de #130 de {@code ImportacionDeNormasServiceTest}. El número de llamadas de {@code
 * SemillaDeDesarrolloRunnerTest} se calcula a partir de los archivos.
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
  private static final int NORMAS = 5;
  private static final int REGLAS = 280;
  private static final int CONSOLIDADOS = 1;
  private static final List<Integer> REGLAS_POR_VERSION = List.of(20, 27, 276, 277, 280);
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
  void tt04_laSemillaCompletaDejaCincoNormasYUnaVersionVigenteConTodasSusReglas() {
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

  @Test
  void tt04_t4_laSemillaTraeLosCasosDeEnt04() {
    cargarLaSemilla();

    List<Regla> reglasVigentes = reglasDeLaVersion(versionVigente());

    assertThat(reglasVigentes)
        .as("una carga mínima por periodo")
        .anyMatch(
            regla ->
                regla.datos() instanceof DatosDeRegla.DeCargaPorPeriodo carga
                    && carga.tipo() == TipoDeCargaPorPeriodo.MINIMA);
    assertThat(reglasVigentes)
        .as("una carga máxima por periodo")
        .anyMatch(
            regla ->
                regla.datos() instanceof DatosDeRegla.DeCargaPorPeriodo carga
                    && carga.tipo() == TipoDeCargaPorPeriodo.MAXIMA);
    assertThat(reglasVigentes)
        .as("un prerrequisito")
        .anyMatch(regla -> regla.datos() instanceof DatosDeRegla.DePrerrequisito);
    assertThat(reglasVigentes)
        .as("un correquisito")
        .anyMatch(regla -> regla.datos() instanceof DatosDeRegla.DeCorrequisito);
    assertThat(reglasVigentes)
        .as("una regla informativa")
        .anyMatch(regla -> regla.clasificacion() == ClasificacionRegla.INFORMATIVA);
    assertThat(reglasVigentes)
        .as("una regla programada")
        .anyMatch(regla -> regla.estado() == EstadoRegla.PROGRAMADA);
    assertThat(reglasVigentes)
        .as("una regla vigente con vigencia hasta")
        .anyMatch(
            regla ->
                regla.estado() == EstadoRegla.VIGENTE
                    && regla.vigenciaPropia().flatMap(Vigencia::vigenteHasta).isPresent());

    Set<CodigoMateria> materiasDeLaVersion = materiasDeLaVersion(reglasVigentes);
    for (Regla regla : reglasVigentes) {
      if (regla.datos() instanceof DatosDeRegla.DeCorrequisito correquisito) {
        assertThat(materiasDeLaVersion)
            .as("el correquisito exige una materia de la malla")
            .contains(correquisito.correquisito().materia());
        assertThat(codigosDe(correquisito.correquisito().expresion()))
            .as("las materias que satisfacen el correquisito")
            .isNotEmpty()
            .allSatisfy(codigo -> assertThat(materiasDeLaVersion).contains(codigo));
      }
    }
  }

  @Test
  void tt04_t4_losDatosDePruebaNoCambianLaVersionDelPlanDeEstudios() {
    cargarLaSemilla();

    List<VersionEntidad> ordenadas = versionesDeSistemas();
    assertThat(ordenadas).hasSize(VERSIONES);
    int versionDelPlan = ordenadas.get(2).getNumeroPlanDeEstudios();
    assertThat(ordenadas.subList(2, VERSIONES))
        .as("las versiones 3, 4 y 5 no cambian la versión del plan de estudios")
        .extracting(VersionEntidad::getNumeroPlanDeEstudios)
        .containsOnly(versionDelPlan);
  }

  private void cargarLaSemilla() {
    new SemillaDeDesarrolloRunner(programas, normas).run(new DefaultApplicationArguments());
  }

  private Cifras cifras() {
    List<VersionEntidad> ordenadas = versionesDeSistemas();
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

  private List<VersionEntidad> versionesDeSistemas() {
    UUID programaId = programaDeSistemas();
    return versiones.findAll().stream()
        .filter(version -> programaId.equals(version.getProgramaId()))
        .sorted(Comparator.comparingInt(VersionEntidad::getNumero))
        .toList();
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

  private List<Regla> reglasDeLaVersion(VersionEntidad version) {
    Map<UUID, EstadoRegla> estados =
        reglasEnVersion.findByVersionId(version.getId()).stream()
            .collect(
                Collectors.toMap(
                    ReglaEnVersionEntidad::getReglaId,
                    fila -> EstadoRegla.valueOf(fila.getEstado())));
    return reglas.findAllById(estados.keySet()).stream()
        .map(entidad -> ReglaMapper.aDominio(entidad, estados.get(entidad.getId())))
        .toList();
  }

  private static Set<CodigoMateria> materiasDeLaVersion(List<Regla> reglas) {
    return reglas.stream()
        .filter(regla -> regla.datos() instanceof DatosDeRegla.DeMateria)
        .map(regla -> ((DatosDeRegla.DeMateria) regla.datos()).materia().codigo())
        .collect(Collectors.toSet());
  }

  private static Set<CodigoMateria> codigosDe(ExpresionDeMaterias expresion) {
    return switch (expresion) {
      case ExpresionDeMaterias.MateriaDelPrograma materia -> Set.of(materia.codigo());
      case ExpresionDeMaterias.MateriaExterna ignored -> Set.of();
      case ExpresionDeMaterias.Y y -> union(codigosDe(y.izquierda()), codigosDe(y.derecha()));
      case ExpresionDeMaterias.O o -> union(codigosDe(o.izquierda()), codigosDe(o.derecha()));
    };
  }

  private static Set<CodigoMateria> union(
      Set<CodigoMateria> izquierda, Set<CodigoMateria> derecha) {
    Set<CodigoMateria> todos = new HashSet<>(izquierda);
    todos.addAll(derecha);
    return todos;
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
