package co.edu.unal.rutaun.normativa.aplicacion;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.unal.rutaun.compartido.CodigoMateria;
import co.edu.unal.rutaun.compartido.EstadoRegla;
import co.edu.unal.rutaun.compartido.NormaId;
import co.edu.unal.rutaun.normativa.ClasificacionRegla;
import co.edu.unal.rutaun.normativa.dominio.DatosDeRegla;
import co.edu.unal.rutaun.normativa.dominio.ExpresionDeMaterias;
import co.edu.unal.rutaun.normativa.dominio.ReglaEnVersion;
import co.edu.unal.rutaun.normativa.dominio.TipoCargaPorPeriodo;
import co.edu.unal.rutaun.normativa.dominio.Vigencia;
import co.edu.unal.rutaun.normativa.persistencia.NormaEntity;
import co.edu.unal.rutaun.normativa.persistencia.NormaRepository;
import co.edu.unal.rutaun.normativa.persistencia.ProgramaRepository;
import co.edu.unal.rutaun.normativa.persistencia.ReglaEnVersionEntity;
import co.edu.unal.rutaun.normativa.persistencia.ReglaEntity;
import co.edu.unal.rutaun.normativa.persistencia.VersionDelConsolidadoRepository;
import co.edu.unal.rutaun.normativa.persistencia.VersionEntity;
import co.edu.unal.rutaun.soporte.PruebaDeIntegracion;
import co.edu.unal.rutaun.soporte.SoporteDeSemilla;
import jakarta.persistence.EntityManager;
import java.io.IOException;
import java.util.ArrayList;
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
 * programas, normas, reglas, versiones y reglas por versión, una y dos veces.
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
  private static final List<Integer> REGLAS_POR_VERSION = List.of(20, 27, 276, 277, 280);
  private static final int VERSIONES = REGLAS_POR_VERSION.size();
  private static final int REGLAS_EN_VERSION =
      REGLAS_POR_VERSION.stream().mapToInt(Integer::intValue).sum();
  private static final int REGLAS_DE_LA_VIGENTE =
      REGLAS_POR_VERSION.get(REGLAS_POR_VERSION.size() - 1);
  private static final Cifras ESPERADAS =
      new Cifras(PROGRAMAS, NORMAS, REGLAS, VERSIONES, REGLAS_EN_VERSION, REGLAS_POR_VERSION);

  private final ImportacionDeProgramasService programas;
  private final ImportacionDeNormasService normas;
  private final ProgramaRepository programasGuardados;
  private final NormaRepository normasGuardadas;
  private final VersionDelConsolidadoRepository versiones;

  @Autowired private EntityManager em;

  /** Recibe los servicios y repositorios que Spring inyecta. */
  @Autowired
  SemillaDeSistemasTest(
      ImportacionDeProgramasService programas,
      ImportacionDeNormasService normas,
      ProgramaRepository programasGuardados,
      NormaRepository normasGuardadas,
      VersionDelConsolidadoRepository versiones) {
    this.programas = programas;
    this.normas = normas;
    this.programasGuardados = programasGuardados;
    this.normasGuardadas = normasGuardadas;
    this.versiones = versiones;
  }

  @BeforeEach
  void comprobarOrigenDeLaSemilla() throws IOException {
    SoporteDeSemilla.comprobarOrigen();
  }

  @Test
  void tt04_laSemillaCompletaDejaCincoNormasYUnaVersionVigenteConTodasSusReglas() {
    cargarLaSemilla();

    Cifras cifras = cifras();
    registrar(cifras);
    assertThat(cifras)
        .as("la semilla cambió (actualiza las constantes) o una prueba dejó filas sin revertir")
        .isEqualTo(ESPERADAS);

    VersionEntity vigente = versionVigente();
    assertThat(vigente.getNumero()).isEqualTo(VERSIONES);
    assertThat(vigente.getReglasEnVersion()).hasSize(REGLAS_DE_LA_VIGENTE);
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

    List<ReglaEnVersion> reglasVigentes = reglasDeLaVersion(versionVigente());

    assertThat(reglasVigentes)
        .as("una malla con materias")
        .anyMatch(regla -> regla.regla().datos() instanceof DatosDeRegla.DeMateria);
    assertThat(reglasVigentes)
        .as("una carga mínima por periodo")
        .anyMatch(
            regla ->
                regla.regla().datos() instanceof DatosDeRegla.DeCargaPorPeriodo carga
                    && carga.tipo() == TipoCargaPorPeriodo.MINIMO);
    assertThat(reglasVigentes)
        .as("una carga máxima por periodo")
        .anyMatch(
            regla ->
                regla.regla().datos() instanceof DatosDeRegla.DeCargaPorPeriodo carga
                    && carga.tipo() == TipoCargaPorPeriodo.MAXIMO);
    assertThat(reglasVigentes)
        .as("un prerrequisito")
        .anyMatch(regla -> regla.regla().datos() instanceof DatosDeRegla.DePrerrequisito);
    assertThat(reglasVigentes)
        .as("un correquisito")
        .anyMatch(regla -> regla.regla().datos() instanceof DatosDeRegla.DeCorrequisito);
    assertThat(reglasVigentes)
        .as("una regla informativa")
        .anyMatch(regla -> regla.regla().clasificacion() == ClasificacionRegla.INFORMATIVA);
    assertThat(reglasVigentes)
        .as("una regla programada")
        .anyMatch(regla -> regla.estado() == EstadoRegla.PROGRAMADA);
    assertThat(reglasVigentes)
        .as("una regla vigente con vigencia hasta")
        .anyMatch(
            regla ->
                regla.estado() == EstadoRegla.VIGENTE
                    && regla.regla().vigenciaPropia().flatMap(Vigencia::vigenteHasta).isPresent());

    Set<CodigoMateria> materiasDeLaVersion = materiasDeLaVersion(reglasVigentes);
    for (ReglaEnVersion enVersion : reglasVigentes) {
      if (enVersion.regla().datos() instanceof DatosDeRegla.DeCorrequisito correquisito) {
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
  void tt04_t4_losDatosDePruebaNoCambianLaVersionDelPlanDeEstudios() throws IOException {
    cargarLaSemilla();

    List<VersionEntity> ordenadas = versionesDeSistemas();
    assertThat(ordenadas).hasSize(VERSIONES);
    int primeraVersionDePrueba = VERSIONES - SoporteDeSemilla.archivosDePrueba().size();
    int versionDelPlan = ordenadas.get(primeraVersionDePrueba - 1).getNumeroPlanDeEstudios();
    assertThat(ordenadas.subList(primeraVersionDePrueba - 1, VERSIONES))
        .as("las versiones con datos de prueba no cambian la versión del plan de estudios")
        .extracting(VersionEntity::getNumeroPlanDeEstudios)
        .containsOnly(versionDelPlan);
  }

  private void cargarLaSemilla() {
    new SemillaDeDesarrolloRunner(programas, normas).run(new DefaultApplicationArguments());
  }

  private Cifras cifras() {
    List<VersionEntity> ordenadas = versionesDeSistemas();
    return new Cifras(
        programasGuardados.count(),
        normasGuardadas.count(),
        reglaCuenta(),
        ordenadas.size(),
        ordenadas.stream().mapToLong(version -> version.getReglasEnVersion().size()).sum(),
        ordenadas.stream().map(version -> version.getReglasEnVersion().size()).toList());
  }

  private UUID programaDeSistemas() {
    return programasGuardados.findByCodigoSia("2879").orElseThrow().getId();
  }

  private List<VersionEntity> versionesDeSistemas() {
    UUID programaId = programaDeSistemas();
    return versiones.findAll().stream()
        .filter(version -> programaId.equals(version.getProgramaId()))
        .sorted(Comparator.comparingInt(VersionEntity::getNumero))
        .toList();
  }

  private VersionEntity versionVigente() {
    return versiones.findTopByProgramaIdOrderByNumeroDesc(programaDeSistemas()).orElseThrow();
  }

  private Set<UUID> identificadores() {
    Set<UUID> ids = new HashSet<>();
    programasGuardados.findAll().forEach(programa -> ids.add(programa.getId()));
    normasGuardadas.findAll().forEach(norma -> ids.add(norma.getId()));
    ids.addAll(em.createQuery("select r.id from ReglaEntity r", UUID.class).getResultList());
    versiones.findAll().forEach(version -> ids.add(version.getId()));
    return ids;
  }

  private Set<UUID> normasDeLasReglasDe(VersionEntity version) {
    Set<UUID> reglaIds =
        version.getReglasEnVersion().stream()
            .map(ReglaEnVersionEntity::getReglaId)
            .collect(Collectors.toSet());
    return normasGuardadas.findDeLasReglas(reglaIds).stream()
        .map(NormaEntity::getId)
        .collect(Collectors.toSet());
  }

  private List<ReglaEnVersion> reglasDeLaVersion(VersionEntity version) {
    Map<UUID, EstadoRegla> estados =
        version.getReglasEnVersion().stream()
            .collect(
                Collectors.toMap(
                    ReglaEnVersionEntity::getReglaId,
                    fila -> EstadoRegla.valueOf(fila.getEstado())));
    List<ReglaEnVersion> reglas = new ArrayList<>();
    for (NormaEntity norma : normasGuardadas.findDeLasReglas(estados.keySet())) {
      NormaId normaId = new NormaId(norma.getId());
      for (ReglaEntity regla : norma.getReglas()) {
        if (estados.containsKey(regla.getId())) {
          reglas.add(
              new ReglaEnVersion(ReglaMapper.aDominio(regla, normaId), estados.get(regla.getId())));
        }
      }
    }
    return reglas;
  }

  private long reglaCuenta() {
    return em.createQuery("select count(r) from ReglaEntity r", Long.class).getSingleResult();
  }

  private static Set<CodigoMateria> materiasDeLaVersion(List<ReglaEnVersion> reglas) {
    return reglas.stream()
        .filter(enVersion -> enVersion.regla().datos() instanceof DatosDeRegla.DeMateria)
        .map(enVersion -> ((DatosDeRegla.DeMateria) enVersion.regla().datos()).materia().codigo())
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
      long versiones,
      long reglasEnVersion,
      List<Integer> reglasPorVersion) {}
}
